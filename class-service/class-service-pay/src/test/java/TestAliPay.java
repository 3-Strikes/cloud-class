import cn.hutool.core.util.IdUtil;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConfig;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import org.junit.jupiter.api.Test;

public class TestAliPay {
    @Test
    public void pay() throws AlipayApiException {
        // 初始化SDK
        AlipayClient alipayClient = new DefaultAlipayClient(getAlipayConfig());

        // 构造请求参数以调用接口
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setReturnUrl("http://www.baidu.com");//支付完成后跳转的页面
        request.setNotifyUrl("http://v7d5646a.natappfree.cc/alipay/callback");//内网穿透：端口映射()
        AlipayTradePagePayModel model = new AlipayTradePagePayModel();

        // 设置商户订单号（自己的订单编号）
        model.setOutTradeNo(IdUtil.getSnowflakeNextIdStr());

        // 设置订单总金额(元)，保留两位小数点
        model.setTotalAmount("88.88");

        // 设置订单标题
        model.setSubject("购买java课程100元");

        // 设置产品码（固定的）
        model.setProductCode("FAST_INSTANT_TRADE_PAY");

        // 设置订单超时时间(m,h,d)
        model.setTimeoutExpress("4m");

        request.setBizModel(model);

        AlipayTradePagePayResponse response = alipayClient.pageExecute(request, "POST");
        String pageRedirectionData = response.getBody();
        System.out.println(pageRedirectionData);

        if (response.isSuccess()) {
            System.out.println("调用成功");
        } else {
            System.out.println("调用失败");
        }
    }

    private static AlipayConfig getAlipayConfig() {
        String privateKey  = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCABi1Zceo/XYsyev0f22TN8DjUI9q/GipqfS6U1MXOg02Icp9ZGkaxwV9WF1WflG4FPlbyxI3oY1Vr6uu19VsFUFlZYFGzlX1L1Gn8zJeexza3m9g54LC58wDjlwj1afwUtj8EX/GWYmfuufvq7qREuo0+2GnjyT6pW8flPt+SpiKFxvzQzrqhAndBZ72rveP0+bRoV8uk1jvCDSrnc/w4fqgbklUd5ZyX82VQve7xbGDiQ5rp6N+/ImoNbOlckU0YXNdY3eb4RNrAfYAdwnEm3M+mPgMdhRLfa12IwABOU4tBVmRIw8uRKriFL59vfY2M9QxcZxXYRPaQiip9fowJAgMBAAECggEAAycjysYhbLoTCYU54qT2kJPP8nmHk5lDXigDE6tBy9oz+J5KClm5T/thK6+bbjirlPl6DrSEgiqgGe5JK8wFF9ZovOjAWlneIhXzHkSznzdsw4zVAGfmMk4nwXMgx1p62M+8MZFjU77MrHZsG9cQuJiXjeqqgO+d2XHnNGAeBXCE/pkTGTRYDrwcKcL/H+5Sef4BhFC4GDkdG5kGDuQbr+lcxOHj1qo5j2GPTaC8Vaf8YIacoPDvNaajlwhBD+L+B8omyS4wQzJ8CTZFRu2bmosa5TpFqmHkURHuEPMHMGLcDkko90ltw+rInaWLNSAnK69g5qr9i2+3dmTfqp6hOQKBgQDOfUW/s/WlPI0pmkGQiRuxQlsis7zZLux/O9Aij8kHRA1QhR0/bksAUwL+8IEz5BZVeSy1bigLg2gNOwB7lwmjYXKGIVBL9BZcwKJgTATGEp01gi8zWjTPxm5N7EDSQGRzqvVy/WixOz/GboebSXYKeDGrHEqb8c4ccCfL1W2hIwKBgQCeuI2om4JH+ilYJETOdJCvol1Mf+iaH2+2ESlrVHgb+GZy1IdejzytJQn8jUbjRwUlqLHWAqSSeZxYwAycss4ZkhajKrWqBfdqsmeTu7ZJqtFMDQhTTqUq1gPcFN5TQzhNBEM9/eP6FR3vKkyFJxUbDqYaL1DI0kdxbMgwbBBO4wKBgEXZOHPdkzW+4t100cLrrlNq59s4Q6SP6+4qNIDMdKshiQN40+j2DSbh7byBbKM5/5gQMmB5D9C9NcBr+gDUma0LtKrMsBXRTpM3knXSoTbDayyDiiXr2LuEGyH+zfqRT6mU5gxszjJNoYglMXgFBIoMGHkSS+auiBM0mTGpU7lTAoGBAI9T3vAsBbjTqXQjxFKfJmzD9PZ45uYOj0VdOAbpD1FKvdDiJJ/6PvOn5929Ag+I+ZfpPzYfytdOpCXsvzbdrImgx3puvf+cRbF/C1N4BgN+EmRXvHXmJfaJrpCV87JCmFvMBqC7XZxeZ1qq2tC+2ytEzjAFHDFM2iEN97TqUMu7AoGAJar71YzXjtD/nLKMEP0L5KBJkJH0eGQRkLbiRBLXryYIIC0BYUzPNgqcwXkB2FQvXifbG0iy6FwCPB7ZX6SwDwa8UjVXhvR36StDaNwcdAsvwTc9KLQwrv36nnYvA35/O91cmLDVnyGXU7DQ9NUJOdL1bTEP+a11yRO5Wca1lkE=";
        String alipayPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAz3LI/LTSys1Y94jJbO39J/5hcHwtKz8fgt85NXWzO18lIROz+/HkfWptC1sxd7t56aSmCD9XdtOf5BNID3RZPqjjOAt5/p9RCFVc1576IBeHkB6xJtdOOqkPzadIfqP2bS4uhTeVvGw7ZtujBwmT7B7nHyneoumBMqVCDjw4JcvmjG+D3lHVomkWcpH7B/7hu+e2awG7GGtbo4q5O08eEZoH0n4NxAKFeF5WtR4DXUTDdR9ORHzP2A2oqYvBtFsLgVe8QA3/6AYLkKI3gJgKVAHpteftGL2UT8+pwsegtVkyLejUDeLDM02fy7qT6pv2SlV0pB0zhoJW5qV+w1JfJwIDAQAB";
        AlipayConfig alipayConfig = new AlipayConfig();
        alipayConfig.setServerUrl("https://openapi-sandbox.dl.alipaydev.com/gateway.do");
        alipayConfig.setAppId("9021000158646207");
        alipayConfig.setPrivateKey(privateKey);
        alipayConfig.setFormat("json");
        alipayConfig.setAlipayPublicKey(alipayPublicKey);
        alipayConfig.setCharset("UTF-8");
        alipayConfig.setSignType("RSA2");
        return alipayConfig;
    }
}
