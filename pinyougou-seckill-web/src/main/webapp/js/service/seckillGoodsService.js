app.service('seckillGoodsService',function ($http) {

    //读取秒杀商品列表并绑定到表单中
    this.findList=function () {
        return $http.get('../seckillGoods/findList.do');
    }
});