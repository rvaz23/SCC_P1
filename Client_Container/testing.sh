#!/bin/bash

artillery run create-users.yml -o users_br.json
artillery run create-channels.yml -o channels_br.json
artillery run create-messages.yml -o messages_br.json
artillery run workload1.yml -o w1_br.json

artillery report --output kub_users_br.html users_br.json
artillery report --output kub_channels_br.html channels_br.json
artillery report --output kub_messages_br.html messages_br.json
artillery report --output kub_w1_br.html w1_br.json     
      

curl -X POST https://content.dropboxapi.com/2/files/upload \
    --header "Authorization: Bearer wD5Fjoe8cDwAAAAAAAAAAVDQURqUQVyIe5po9f7m_PW_OBUBRncvDof9_qA3Zsit" \
    --header "Dropbox-API-Arg: {\"path\": \"/kub_users_br.html\"}" \
    --header "Content-Type: application/octet-stream" \
    --data-binary @kub_users_br.html
    
curl -X POST https://content.dropboxapi.com/2/files/upload \
    --header "Authorization: Bearer wD5Fjoe8cDwAAAAAAAAAAVDQURqUQVyIe5po9f7m_PW_OBUBRncvDof9_qA3Zsit" \
    --header "Dropbox-API-Arg: {\"path\": \"/kub_channels_br.html\"}" \
    --header "Content-Type: application/octet-stream" \
    --data-binary @kub_channels_br.html 
    
curl -X POST https://content.dropboxapi.com/2/files/upload \
    --header "Authorization: Bearer wD5Fjoe8cDwAAAAAAAAAAVDQURqUQVyIe5po9f7m_PW_OBUBRncvDof9_qA3Zsit" \
    --header "Dropbox-API-Arg: {\"path\": \"/kub_messages_br.html\"}" \
    --header "Content-Type: application/octet-stream" \
    --data-binary @kub_messages_br.html  
    
curl -X POST https://content.dropboxapi.com/2/files/upload \
    --header "Authorization: Bearer wD5Fjoe8cDwAAAAAAAAAAVDQURqUQVyIe5po9f7m_PW_OBUBRncvDof9_qA3Zsit" \
    --header "Dropbox-API-Arg: {\"path\": \"/kub_w1_br.html\"}" \
    --header "Content-Type: application/octet-stream" \
    --data-binary @kub_w1_br.html   
    
    


