# ByDesign Microservice

In this project the microservice is built with integrating a onnector ad a bridge. 
Built in Java v.11, the connector allows access to services and information residing
in an SAP ERP (SAP ByDesign product). 
The connector utilizes the OData protocol to query and manipulate ERP data. 
Additionally, a bridge uses the Spring framework to interact with an MQTT 
(Message Queuing Telemetry Transport) broker and expose the services, 
mediated by the connector, to other systems.

## Examples of Possible Usage
Through OData, it is possible to query SAP products, sales orders, invoices, warehouse stock, etc., which a company has recorded in the ERP. This information can be made available to services registered on MQTT, such as billing services, quality controls, e-commerce, etc.

## MQTT
MQTT (Message Queuing Telemetry Transport) is a lightweight messaging protocol based on 
a publish/subscribe model, allowing devices to publish messages on specific
topics and recipients to subscribe to receive them.

![MQTT](/sapbydesign/src/main/resources/MQTT.png)
## OData

OData (Open Data Protocol) is an open protocol that allows the creation and consumption of RESTful APIs that are queryable and interoperable in a simple and standardized way. The retrieval and modification of data are performed using URL-based service calls.
## SAP and OData

SAP allows the creation of OData services based on its Business Objects, generating a metadata representation in XML format.
## Generation of Java Client and Connector

The metadata is used to generate, in this case, a Java client that represents SAP Business Objects and their connected services. This process automates the client's creation, making the system easily adaptable to any changes in the ERP's data model. The connector created in the project uses the client (representation of SAP BOs and connected services) to access the ERP and query/modify the system through RESTful calls with authentication.
## Integration in MQTT

The bridge created in the project uses the Spring framework to register with an MQTT broker and interact with it asynchronously. The information retrieved from the ERP is sent to the broker through messages, and vice versa, the broker can request the bridge to access services exposed by SAP. The bridge's task is also to translate the client objects into standardized structures on the broker, thereby standardizing communications between the different services registered on the broker.


#### Microservice overview
![Microservice](/sapbydesign/src/main/resources/Connettore.png)


