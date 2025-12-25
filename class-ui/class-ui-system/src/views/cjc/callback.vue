<template>
  <div>

  </div>
</template>

<script>
import axios from "axios";

export default {
  data() {
    return {

    }
  },
  methods: {
    getToken(code){
      let url=axios.defaults.baseURL+"/uaa/oauth2/token";
      const params = new URLSearchParams({
        grant_type: "authorization_code",
        code: code,
        client_id: "admin",
        client_secret: "123",
        redirect_uri: "http://127.0.0.1:6001/callback",
        tempToken:localStorage.getItem("tmpToken")
      });
      this.$http.post(url,params).then(res=>{
        localStorage.removeItem("tmpToken");
        var token = res.data.access_token;
        var refresh_token = res.data.refresh_token;
        var expiresTime =new Date().getTime()+ res.data.expires_in*1000;

        localStorage.setItem("U-TOKEN",token);
        localStorage.setItem("R-TOKEN",refresh_token);
        localStorage.setItem("expiresTime",expiresTime);
        this.$router.push({ path: '/echarts'});
      })
    }
  },
  mounted() {
    //获取url后?拼接的获取授权码
    var query = this.$route.query;
    if(query && query.code){
      this.getToken(query.code);
    }else{
      this.$router.replace({ path:"/login" });
    }
  }
}
</script>

<style>

</style>