app.service('cartService',function ($http) {

    this.findCartList=function () {
        return $http.get('../cart/findCartList.do');
    }
    
    //添加商品到购物车
    this.addGoodsToCartList=function (itemId,num) {
        return $http.get('../cart/addGoodsToCartList.do?itemId='+itemId+'&num='+num);
    }

    //求总价
    this.sum=function (cartList) {
        var totalValue = {totalNum:0,totalMoney:0.00};
        for(var i=0;i<cartList.length;i++){
            var cart = cartList[i];
            for(var j=0;j<cart.orderItemList.length;j++){
                var orderItem = cart.orderItemList[j];
                totalValue.totalNum+=orderItem.num;
                totalValue.totalMoney+=orderItem.totalFee;
            }
        }
        return totalValue;
    }

    //获取地址列表
    this.findAddressList=function () {
        return $http.get('../address/findListByLoginUser.do');
    }

    //保存订单
    this.submitOrder=function (order) {
        return $http.post('../order/add.do',order);
    }
});