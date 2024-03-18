# EQT Code Test - Michael Mrazek

# TL;DR

### Clone the repo
```
git clone git@github.com:wingnut/portfolio-company-enrichment.git
```

### Navigate to project root
```
cd portfolio-company-enrichment
```

### (Java 17 and >= 4G RAM is required)
<span style="color:red">Java 17 is required</span> Tip: Use https://sdkman.io/ for installing multiple java versions and switching between them with ease.
Verify that JAVA_HOME is really pointing to a Java 17 binary: `echo $JAVA_HOME`.

AND/OR

```
java -version
openjdk version "17.0.10" 2024-01-16
OpenJDK Runtime Environment Temurin-17.0.10+7 (build 17.0.10+7)
OpenJDK 64-Bit Server VM Temurin-17.0.10+7 (build 17.0.10+7, mixed mode)
```



### Building (from project root, using the bundled maven)
```
./mvnw clean package
```

### Running (from project root)
```
java -jar -Xmx4g target/portfolio-company-enrichment-1.0-SNAPSHOT-jar-with-dependencies.jar download
```
#### Subsequent (re)runs can skip the "download" parameter to the app
```
java -jar -Xmx4g target/portfolio-company-enrichment-1.0-SNAPSHOT-jar-with-dependencies.jar
```
A typical run last for about a minute or two on my MacBook

Final enriched output file can be found under: `tmp/final-enriched-portfolio-companies.json.gz` unless otherwise configured

# Longer explanation/reasoning
This project is meant as a foundation for discussion in the code test review. There are some areas of a real production system that I have skipped, such as infra structure for running in the cloud etc.
More details below.

### Improvements
- [X] Download all files in parallel in main() before creating and running pipeline
- [X] Filter org enrichment data first to only include the orgs in the portfolio (for memory reasons)
- [ ] Download "divestment" companies too and concatenate with portfolio companies
  - Either use "discriminator column" for divestment or separate record, only one extra field for divestments vs portfolio companies
- [ ] Download compressed funds reference data and use as enrichment for funds list in portfolio company
  - Also enrich with stuff from each individual fund page??? Seems like overkill.
    - The resulting data set would blow up in size due to the long description of the fund
    - DirectRunners (local dev) don't support downloading over http, so it would get messy in local dev

## Next steps/thoughts for a real production pipeline
- Logging (The warnings for: `SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder"`can be safely ignored, I'm not using any logging framework such as logback.)
- Error handling
- Deploy and run with a "production" runner instead of DirectRunner (means we can access files directly over http), Spark, Flink, Google Cloud Dataflow etc 
- Upload resulting file to a GCP bucket
- Dockerize the app
- Set up infrastructure to run using AirFlow?
- Alternatives for resolving the join:
  - Use some sort of Named-entity recognition (NER). Right now relying on: `portfoliocompany.title.toLowerCase == org.name.toLowerCase`, which is risky. Best (of course) would be to make the service exposing the uuid for the portfolio company in the JSON. That way the join could be done more naturally and safer using the uuid in both datasets. NER-services typically means additional costs.
  - Use some sort of fuzzy search/match
  - Since this is an example and the data is downloaded from EQT, one could argue that the data should be provided with uuid. However, the scenario in production is likely that we are mining **other** websites (where we cannot control the api) for potential future portfolio companies, so we likely need some sort of NER functionality to make better/safer matches (perhaps with some confidence score to indicate to the consumer of our app how certain we are that we have the correct enriched data).

#### Some examples where the data quality will bleed over to the final enriched data set
- The "Magnit" company appears with the same title more than once in the reference dataset for orgs, this makes it the join non-deterministic
- The name "Anticimex" in the web json is not consistent with the title "Anticimex AB" in the reference dataset for orgs
- Variations on spelling such as:
  - "AM Pharma" != "AM-Pharma"
  - "Colisee" != "Colisée"


