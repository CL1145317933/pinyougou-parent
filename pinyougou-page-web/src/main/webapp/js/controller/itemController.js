app.controller('itemController',function($scope,$http){
    //商品详细页（控制层）
    $scope.addNum=function(x){
        $scope.num=$scope.num+x;
        if($scope.num<1){
            $scope.num=1;
        }
    }

    $scope.specificationItems={};//记录用户选择的规格

    //用户选择规格
    $scope.selectSpecification=function(name,value){
        $scope.specificationItems[name]=value;
        searchSku();
    }

    //判断某规格是否被用户选中
    $scope.isSelected=function(name,value){
        if($scope.specificationItems[name]==value) {
            return true;
        }
        return false;
    }

    //加载默认的SKU

    $scope.loadSKU=function(){
        $scope.sku=skuList[0];
        $scope.specificationItems=JSON.parse(JSON.stringify($scope.sku.spec));
    }

    //匹配两个对象
    matchObject=function(map1,map2){

        for(var key1 in map1){
            if(map1[key1]!=map2[key1]){
                return false;
            }
        }

        for(var key2 in map2){
            if(map2[key2]!=map1[key2]){
                return false;
            }
        }

        return true;
    }

    //查询SKU
    searchSku=function(){
        for(var i=0;i<skuList.length;i++){
            if(matchObject(skuList[i].spec,$scope.specificationItems)){
                $scope.sku=skuList[i];
                return;
            }
        }
        $scope.sku={id:0,title:'-----',price:0};//没有匹配成功

    }

    //添加商品到购物车
    $scope.addToCart=function(){
        $http.get('http://localhost:9107/cart/addGoodsToCartList.do?itemId='+$scope.sku.id +'&num='+$scope.num
        ,{'withCredentials':true}).success(
            function (response) {
                if(response.flag) {
                    location.href="http://localhost:9107/cart.html";//跳转到购物车页面
                }else{
                    alert(response.message);
                }
            }
        );
    }

});