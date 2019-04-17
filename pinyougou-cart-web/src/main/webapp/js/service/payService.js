app.service('payService',function ($http) {

    //生成二维码
    this.createNative=function () {
        return $http.get('../pay/createNative.do');
    }
    
    //检测支付状态
    this.queryPayStatus=function (out_trade_no) {
        return $http.get('../pay/queryPayStatus.do?out_trade_no='+out_trade_no);
    }
});