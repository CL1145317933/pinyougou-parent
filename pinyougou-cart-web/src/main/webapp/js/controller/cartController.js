app.controller('cartController',function ($scope,cartService) {

    //查询购物车列表
    $scope.findCartList=function () {
        cartService.findCartList().success(
            function (response) {
                $scope.cartList=response;
                //求购物车商品总金额
                $scope.totalValue=cartService.sum($scope.cartList);
            }
        );
    }

    //添加商品到购物车
    $scope.addGoodsToCartList=function (itemId,num) {
        cartService.addGoodsToCartList(itemId,num).success(
            function (response) {
                if(response.flag){
                    $scope.findCartList();
                }else {
                    alert(response.message);
                }
            }
        );
    }

    //获取地址列表
    $scope.findAddressList=function () {
        cartService.findAddressList().success(
            function (response) {
                $scope.addressList=response;

                //设置默认地址
                for(var i=0;i<$scope.addressList.length;i++){
                    if($scope.addressList[i].isDefault=='1'){
                        $scope.address=$scope.addressList[i];
                        break;
                    }
                }
            }
        );
    }
    
    //选择地址
    $scope.selectAddress=function (address) {
        $scope.address=address;
    }

    //判断是否是当前选中的地址
    $scope.isSelectAddress=function (address) {
        if(address==$scope.address){
            return true;
        }else{
            return false;
        }
    }

    $scope.order={paymentType:'1'};
    //选择结算方式
    $scope.selectPayType=function (type) {
        $scope.order.paymentType=type;
    }

    //提交保存订单
    $scope.submitOrder=function () {
        $scope.order.receiverAreaName=$scope.address.address;//地址
        $scope.order.receiverMobile=$scope.address.mobile;//手机
        $scope.order.receiver=$scope.address.contact;//联系人
        cartService.submitOrder($scope.order).success(
            function (response) {
                if(response.flag){
                    //页面跳转
                    if($scope.order.paymentType=='1'){//微信支付
                        location.href="pay.html";
                    }else{//货到付款
                        location.href="paysuccess.html";
                    }
                }else{
                    alert(response.message);//
                }
            }
        );
    }

});