# ByDesign Microservice

In this project, the microservice is built by integrating a connector ad a bridge. 
Developed in Java v.11, the connector allows access to services and information 
in an SAP ERP (SAP ByDesign product). 
The connector utilizes the OData protocol to query and manipulate ERP data. 
Additionally, a bridge uses the Spring framework to interact with an MQTT 
(Message Queuing Telemetry Transport) broker and expose the services, 
mediated by the connector, to other systems.




