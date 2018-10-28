 //���Ʋ� 
app.controller('typeTemplateController' ,function($scope ,$controller ,typeTemplateService ,brandService ,specificationService){
	
	$controller('baseController',{$scope:$scope});//�̳�
	
    //��ȡ�б����ݰ󶨵�����  
	$scope.findAll=function(){
		typeTemplateService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//��ҳ
	$scope.findPage=function(page,rows){			
		typeTemplateService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//�����ܼ�¼��
			}			
		);
	}
	
	//��ѯʵ�� 
	$scope.findOne=function(id){				
		typeTemplateService.findOne(id).success(
			function(response){
				$scope.entity= response;
				$scope.entity.brandIds=JSON.parse($scope.entity.brandIds);
				$scope.entity.specIds=JSON.parse($scope.entity.specIds);
				$scope.entity.customAttributeItems=JSON.parse($scope.entity.customAttributeItems);
			}
		);				
	}
	
	//���� 
	$scope.save=function(){				
		var serviceObject;//��������  				
		if($scope.entity.id!=null){//�����ID
			serviceObject=typeTemplateService.update( $scope.entity ); //�޸�  
		}else{
			serviceObject=typeTemplateService.add( $scope.entity  );//���� 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//���²�ѯ 
		        	$scope.reloadList();//���¼���
					$scope.selectIds=[];
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//����ɾ�� 
	$scope.dele=function(){			
		//��ȡѡ�еĸ�ѡ��			
		typeTemplateService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//ˢ���б�
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//������������ 
	
	//����
	$scope.search=function(page,rows){			
		typeTemplateService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//�����ܼ�¼��
			}			
		);
	}

    $scope.brandList={data:[]};//Ʒ���б�

	$scope.findBrandList=function () {
		brandService.selectOptionList().success(
			function (response) {
				$scope.brandList={data:response};
            }
		);
    }

    $scope.specList={data:[]};//����б�
	//���ҹ���б�
	$scope.findSpecList=function () {
        specificationService.selectOptionList().success(
            function (response) {
                $scope.specList={data:response};
            }
        );
    }

    //�����չ������
	$scope.addTableRow=function () {
		$scope.entity.customAttributeItems.push({});
    }

    //ɾ����չ������
	$scope.deleTableRow=function (index) {
        $scope.entity.customAttributeItems.splice(index,1);
    }
});
