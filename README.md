# SCC_P1

-Rodrigo Gomes - 52596 - ru.gomes@campus.fct.unl.pt
-Ruben Vaz - 52656 - ra.vaz@campus.fct.unl.pt
-Sergio Seco - 59286 - s.seco@campus.fct.unl.pt


Cognitive Search igual ao das aulas utilizado para uma pesquisa personalizada no texto das mensagens.

Chaves de conexão ao cognitive search estão harcoded, pois as variaveis de ambiente estavam a dar um erro.

Para o functions para replicar conteudos a variavel de ambiente esperada é BLOB_WESTUS e BLOB_NEURO

Deployment:
build image: docker build -t nunopreguica/scc2122-app dir
push to dockerHub: docker push nunopreguica/scc2122-app
run image locally: docker run --rm -p 8080:8080 nunopreguica/scc2122-app
create resource group: az group create --name scc2122-cluster-4204 --location westeurope
start container: az container create --resource-group scc2122-cluster-4204 --name scc-app --image nunopreguica/scc2122-app --ports 8080 --dns-name-label scc-discord-4204
delete container: az container delete --resource-group scc2122-cluster-4204 --name scc-app