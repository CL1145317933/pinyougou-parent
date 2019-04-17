app.controller('payController',function ($scope,$location,payService) {

    //本地生成二维码
    $scope.createNative=function () {
        payService.createNative().success(
            function (response) {
                $scope.money=(response.total_fee/100).toFixed(2);//应付金额
                $scope.out_trade_no=response.out_trade_no;//订单号
                //二维码
                var qr = new QRious({
                    element:document.getElementById('qrious'),
                    size:250,
                    level:'H',
                    value:response.code_url
                });

                $scope .queryPayStatus($scope.out_trade_no);
            }
        );
    }
    
    $scope.queryPayStatus=function (out_trade_no) {
        payService.queryPayStatus(out_trade_no).success(
            function (response) {
                if (response.flag){
                    location.href="paysuccess.html?money="+$scope.money;
                }else{
                    if(response.flag=='二维码超时'){
                        $scope.createNative();
                    }else{
                        location.href="payfail.html";
                    }

                }
            }
        );
    }
    
    $scope.getMoney=function () {
        return $location.search()['money'];
    }
});