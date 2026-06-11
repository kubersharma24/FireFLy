# FireFLy

====================================
Kafka
====================================
kafka - Kafka 4.2.1

PS D:\kafka> .\bin\windows\kafka-storage.bat random-uuid
Cid - 5DymFTvXSsOltT5Z7z1UFw

old Cluster removed
PS D:\kafka> Remove-Item -Recurse -Force D:\tmp\kraft-combined-logs -ErrorAction SilentlyContinue
PS D:\kafka> dir D:\tmp
PS D:\kafka> Remove-Item -Recurse -Force D:\tmp -ErrorAction SilentlyContinue
PS D:\kafka> cd D:\kafka

format disk
PS D:\kafka> .\bin\windows\kafka-storage.bat format -t 5DymFTvXSsOltT5Z7z1UFw -c .\config\server.properties --standalone

start broker 
.\bin\windows\kafka-server-start.bat .\config\server.properties


reset ofset 
# Reset offset to latest (skip all existing messages)
.\bin\windows\kafka-consumer-groups.bat --bootstrap-server localhost:9092 --group email-group --topic email --reset-offsets --to-latest --execute

verify reset 
.\bin\windows\kafka-consumer-groups.bat --bootstrap-server localhost:9092 --group email-group --describe



====================================
Env Settings
====================================
$env:LLM_PROVIDER = "groq"
$env:GROQ_API_KEY = "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-poiuytrrtyuiopoiuy"
$env:MAIL_USERNAME = "kubersharma1549@gmail.com"
$env:MAIL_PASSWORD = "oqzvjimnmovdgiko-poiuytryuio-oiuytryuiop"


=================================
post man 
================================
https://documenter.getpostman.com/view/29809570/2sBXwsLVXd
