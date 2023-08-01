# SAPByDBridge


In questo progetto  un connettore realizzato in Java (versione 11)  permette di accedere ai servizi ed alle informazioni residenti in un ERP SAP (prodotto SAP ByDesign). Il connettore utilizza il protocollo OData per interrogare e manipolare i dati dell'ERP. Inoltre un bridge utilizza il framework Spring per interagire con un broker MQTT (Message Queuing Telemetry Transport) ed esporre i servizi, mediati dal connettore,verso altri sistemi . 



## OData

OData (Open Data Protocol) è un protocollo aperto che consente la creazione e il consumo di API RESTful interrogabili e interoperabili in modo semplice e standardizzato. Il recupero e la modifica dei dati viene eseguita con chiamate di servizio basate su URL.

## SAP e OData

SAP permette di creare dei servizi OData a partire dai suoi Business Object, generandone una rappresentazione metadata in formato XML. 

## Generazione del Client Java e del connettore 

I metadata sono utilizzati per generare, in questo caso, un client Java che rappresenta i Business Object SAP ed i servizi connessi.
Questo processo automatizza la creazione del client, rendendo il sistema facilmente adattabile a qualsiasi cambiamento nel modello dei dati dell'ERP.
Il connettore creato nel progetto utilizza il client ( rappresentazione  dei BO SAP ed i servizi connessi) per accedere all' ERP ed interrogare/modificare il sistema tramite chiamate RESTFull con autenticazione. 

## Integrazione con MQTT

Il bridge creato nle progetto utilizza il framework Spring per registrarsi ad un broker MQTT ed interagire con esso in modalità asincrona. Le informazioni  recuperate dall'ERP vengono inviate al broker tramite messaggi e viceversa il broker può chiedere al bridge di accedere ai servizi esposti da SAP. Il compito del bridge è anche quello di tradurre gli oggetti del client in stutture normalizzate sul broker uniformando le comunicazioni tra i diversi servizi registrati sul broker.

## Esempi di possibili utilizzo 

Tramite OData è possibile interrogare in SAP prodotti, ordini di vendita, fatture, giacenze magazzino ecc. che un'azienda ha registrate sull'ERP. Queste informazioni possono essere messe a disposizione di servizi registrati sull'MQTT, come servizi di fatturazione, controlli di qualità, ecommerce ecc.


## Licenza

// Inserisci qui le informazioni sulla licenza del tuo progetto.
