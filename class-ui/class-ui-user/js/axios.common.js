import routes from "../../class-ui-system/src/routes";
import VueRouter from "vue-router";

axios.defaults.baseURL = "http://127.0.0.1:10010/ymcc"//配置前缀
Vue.prototype.$http = axios //给Vue这个类添加一个原型的属性,这个类的对象都能调用
Vue.config.productionTip = false

const router = new VueRouter({
    mode: 'history',
    routes
})


var api = {
    getQueryVariable(variable){
        var query = window.location.search.substring(1);
        var vars = query.split("&");
        for (var i=0;i < vars.length;i++) {
            var pair = vars[i].split("=");
            if(pair[0] == variable){return pair[1];}
        }
        return(false);
    },
    getCallUrl(){
        let callUrlName = "callUrl";
        var href = window.location.href;
        if(href.indexOf(callUrlName) > 0){
            return href.substring(href.indexOf(callUrlName)+callUrlName.length+1);
        }
    },
    toLogin(callUrl){
        window.location.href="http://127.0.0.1:6003/login.html?callUrl="+callUrl;
    },
    noPermission() {
        alert("没访问权限");
    },
    logOut(){
        $.cookie("U-TOKEN",null);
        $.cookie("R-TOKEN",null);
        $.cookie("user",null);
    }
};

let noNeedLoginUrl = ["login.html","reg.phone.html"];

//url放行
async function getNewToken() {
    var refreshToken = localStorage.getItem('R-TOKEN');
    let user = JSON.parse(localStorage.getItem("user"));

    if(refreshToken){
        return await axios({
            url: '/uaa/login/refresh',
            method: 'post',
            data:{
                refreshToken:refreshToken,
                username: user.username
            }
        })
    }else{
        toLogin();
    }
}

async function doRequest () {
    try {
        //获取新的Token
        const data = await getNewToken();
        let str=data.data.data;
        let result=JSON.parse(str);

        var token = result.access_token;
        var refresh_token = result.refresh_token;
        var expiresTime =new Date().getTime()+ result.expires_in*1000;
        localStorage.setItem("U-TOKEN",token);
        localStorage.setItem("R-TOKEN",refresh_token);
        localStorage.setItem("expiresTime",expiresTime);
        // var token = data.data.data.access_token;
        // var refresh_token = data.data.data.refresh_token;
        // var expiresTime = data.data.data.expiresTime;
        //
        // localStorage.setItem("expiresTime",expiresTime);
        // localStorage.setItem("U-TOKEN",token);
        // localStorage.setItem("R-TOKEN",refresh_token);
        //继续执行上一次失败的请求
    } catch(err) {
        toLogin();
    }
}

axios.interceptors.request.use(config => {

    if(localStorage.getItem('U-TOKEN')){
        // 让每个请求携带token--['X-Token']为自定义key 请根据实际情况自行修改
        config.headers['Authorization'] = "Bearer "+localStorage.getItem('U-TOKEN')
    }

    //刷新Token请求放行
    if(config.url && config.url.indexOf("refresh") > 0){
        return config;
    }

    //如果已经登录了,每次都把token作为一个请求头传递过程
    if (localStorage.getItem('U-TOKEN')) {
        //自动刷新Token
        var expiresTime = localStorage.getItem("expiresTime");

        let nowTime = new Date().getTime();
        let diff = (expiresTime - nowTime) / 1000 / 60;
        if(diff == 0 || diff < 5){
            console.log("Token过期或即将过期...");
            //刷新Token
            doRequest();
        }
    }
    return config
}, error => {
    // Do something with request error
    Promise.reject(error)
})

axios.interceptors.response.use(config => {
    return config
},error => {
    if (error && error.response) {
        if(error.response.status == 401 && error.response.data
            && error.response.data.error && error.response.data.error == "invalid_token"){

            //处理放行
            let currentPage = window.location.href;
            let canPass = false;
            for(let i = 0 ; i < noNeedLoginUrl.length ; i++){
                if(currentPage.indexOf(noNeedLoginUrl[i]) > 0){
                    canPass = true;
                    break;
                }
            }
            if(!canPass){
                alert("登录失效，请先登录");
                let callUrl = window.location.href;
                return api.toLogin(callUrl);
            }

            //token过期情况
            if(error.response.data.error_description && error.response.data.error_description.indexOf("Access token expired")){
                //无感刷新 TODO
                console.log("token过期，刷新Token");
                api.logOut();
            }
        }
        if(error.response.status == 403){
            api.noPermission();
        }
    }
    Promise.reject(error)
});

router.beforeEach((to, from, next) => {

    if (to.path == '/login') {
        //重新登录,把原来session移除掉
        localStorage.removeItem('user');
        localStorage.removeItem('U-TOKEN');
        localStorage.removeItem('R-TOKEN');
        localStorage.removeItem('expiresTime');
    }

    //从session获取用户
    // localStorage.getItem('user'):localStorage获取user
    let user = JSON.parse(localStorage.getItem('user'));
    // callback路径需要放行，因为它是OAuth2回调处理页面
    if (!user &&(to.path != '/login' && to.path != '/register' && to.path != '/callback') ) {
        //没有获取到,跳转登录路由地址
        next({ path: '/login' })
    } else {
        //已经登录,正常访问
        next()  // 访问成功了，放行。。
    }
})