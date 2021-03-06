#### Software richiesto
* Maven 3.1 o superiore (mvn -V)
* MySQL 5.6 o superiore / PostreSQL 9.4

#### 1. Repository

Il contenuto della cartella OpenOLAT è la repository che bisogna importare dentro Eclipse.

Importare la libreria jpl.jar presente nella cartella prolog nella repository locale di Maven
```bash
mvn install:install-file -Dfile=<percorso libreria jpl.jar> -DgroupId=org.jpl7 -DartifactId=jpl -Dversion=1.0 -Dpackaging=jar -DgeneratePom=true
```

Se le variabili M2_REPO mancano in Eclipse eseguire i comandi Maven:

```bash
mvn -Declipse.workspace=<percorso workspace eclipse> eclipse:configure-workspace

mvn eclipse:clean eclipse:eclipse
```

Fare il Refresh del progetto su Eclipse. 

Modificare il file `olat.local.properties` con i parametri relativi al vostro progetto. E' possibile esplorare il fire
`olat.list.properties` per vedere gli altri parametri configurabili.


Nel progetto eclipse trovare i link `src/main/java/olat.local.properties` e `src/test/java/olat.local.properties` e collegarli al file `olat.local.properties` personalizzato in precedenza.
  

#### 2. Database

*PostgreSQL*: Creare un user e un database.
 
*MySQL*: Creare un user e un database.

```sql
CREATE DATABASE IF NOT EXISTS openolat;
GRANT ALL PRIVILEGES ON openolat.* TO 'openolat' IDENTIFIED BY 'openolat';
UPDATE mysql.user SET HOST='localhost' WHERE USER='openolat' AND HOST='%';
FLUSH PRIVILEGES;
```
 
Caricare lo schema del DB base oppure solo per MySQL già popolato.

Se si utilizza lo schema base del DB la cartella openData verrà creata al primo avvio del software, invece nel caso del database già popolato sarà necessario copiare la cartella openData presente in questo progetto perché contiene i file fisici a cui il database fa riferimento.

* Mysql

	```bash
	mysql -u openolat -p openolat < src/main/resources/database/mysql/setupDatabase.sql
	```
* Postgres

	```bash
	psql -U openolat -d openolat < src/main/resources/database/postgres/setupDatabase.sql
	```

Lo schema MySQL già popolato si trova nella cartella dbDump (ricordarsi di utilizzare la cartella openData del progetto).
* Mysql

	```bash
	mysql -u openolat -p openolat < dbDump/openolatdb.sql
	```

#### 3. Tomcat server in Eclipse

Tasto destro sul progetto Eclipse -> Run As -> "Run on Server".
Creare un nuovo server Tomcat 9.0.

Modificare le impostazioni del server appena craeto aumentando i due timeout a 300s e 30s, nelle configurazioni di lancio aggiungendo alla voce Arguments aggiungere:

```
-XX:+UseG1GC -XX:+UseStringDeduplication -Xms256m -Xmx1024m -Djava.awt.headless=true
```

Modificare i file server generati:
* context.xml: settare nell'elemento Context
```
  <Resources cacheMaxSize="51200" />
```

* server.xml: Elemento Context settare il parametro reloadable="false" 


	Elementi Connector settare il parametro URIEncoding="UTF-8"

	Aggiungere uno dei seguenti Elementi Resource, in base al database in utilizzo, nell'elemento GlobalNamingResources.
	Modificare in base ai parametri username password e nome db scelti.

	*MySQL:*

	```xml
	<Resource auth="Container" driverClassName="com.mysql.cj.jdbc.Driver" type="javax.sql.DataSource"
          maxIdle="4" maxTotal="16" maxWaitMillis="10000"
          name="jdbc/OpenOLATDS"
          password="openolat" username="openolat"
          url="jdbc:mysql://localhost:3306/openolat?useUnicode=true&amp;characterEncoding=UTF-8&amp;cachePrepStmts=true&amp;cacheCallableStmts=true&amp;autoReconnectForPools=true"
          testOnBorrow="true" testOnReturn="false"
          validationQuery="SELECT 1" validationQueryTimeout="-1"/>
	```

	*PostreSQL:*

	```xml
	<Resource auth="Container" driverClassName="org.postgresql.Driver" type="javax.sql.DataSource"
          maxIdle="4" maxTotal="16" maxWaitMillis="-1"
          name="jdbc/OpenOLATPostgresDS"
          username="postgres" password="postgres"
          url="jdbc:postgresql://localhost:5432/olat"
          testOnBorrow="true" testOnReturn="false"
          validationQuery="SELECT 1" validationQueryTimeout="-1"/>
	```

#### 4. Run
Ora il server dovrebbe partire sull'indirizzo [http://localhost:8080/olat](http://localhost:8080/olat).

Account amministratore : "administrator" - "openolat".

Account docente : "author" - "test".

Account studente : "test" - "test".

Account studente : "test1" - "test".

Account studente : "test2" - "test".

#### Problemi

* Il server potrebbe dare problemi di Timezone, basta settarne una qualsiasi.

	```sql
	SET GLOBAL time_zone = 'Europe/Zurich';
	```

* OutOfMemoryException: Controllare di aver inserito i valori nel campo Arguments del server Run > Run Configurations > Arguments > VM Arguments: `-XX:+UseG1GC -XX:+UseStringDeduplication -Xms256m -Xmx1024m -Djava.awt.headless=true`

* Timeout Exception: Aumentare il tempo di Timeout del server Tomcat.

* Se Tomcat si avvia ma non trova OpenOlat il server partirà molto velocemente senza dare errori, ma ovviamente l'applicazione non sarà partita: Tasto destro sul server e cliccare "Publish".

* Se compaiono problemi a Runtime di "ClassNotFoundException": Fare il "Clean" sul progetto e sul server e rieffettuare la fase di build di tutti il progetto, riavviare tutto il progetto.

* Warning: The predicates below are not defined: Il file prolog fatti.pl non è stato generato correttamente oppure il database è ancora vuoto (non ci sono corsi e interazioni degli utenti con i corsi).

* PrologException: existence_error: Copiare i file prolog .pl dalla cartella prolog alla cartella openData creata sul server dopo il primo avvio oppure il database è ancora vuoto (non ci sono corsi e interazioni degli utenti con i corsi). 