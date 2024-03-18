# EQT Code Test - Michael Mrazek

### Building (from project root)
```
./mvnw clean package
```

### Running (from project root) (Java 17 is required)
<span style="color:red">Java 17 is required</span>
```
java -jar target/portfolio-company-enrichment-1.0-SNAPSHOT-jar-with-dependencies.jar
```

### Next Steps
- [X] Download all files in parallel in main() before creating and running pipeline
- [X] Filter org enrichment data first to only include the orgs in the portfolio (for memory reasons)
- [X] Run with real full dataset to spot any outliers

- [ ] Download "divestment" companies too and concatenate with portfolio companies
  - Use a flag for divestment or separate record?
  - NOTE: one extra field: exitDate

- [ ] Download compressed funds reference data and use as enrichment for funds list in portfolio company
  - Also enrich with stuff from each individual fund page??? Seems like overkill.
    - The resulting data set would blow up in size due to the long description of the fund
    - DirectRunners (local dev) don't support downloading over http, so it would get messy in local dev

## Next logical steps for a real productio pipeline
- Deploy and run with a "production" runner instead of DirectRunner (means we can access files directly over http), Spark, Flink, Google Cloud Dataflow etc 
- Upload resulting file to a GCP bucket
- Dockerize the app
- Set up infrastructure to run using AirFlow?
- Use some sort of Named-entity recognition (NER) for resolving the join. Right now relying on title == name is risky. Best (of course) would be to make the service exposing the uuid for the portfolio company in the JSON. That way the join could be done more naturally and safer using the uuid in both datasets.

#### Some examples where the data quality will bleed over to the final enriched data set
- The "Magnit" company appears with the same title more than once in the reference dataset for orgs, this makes it the join non-deterministic
- The name "Anticimex" in the web json is not consistent with the title "Anticimex AB" in the reference dataset for orgs
- Variations on spelling such as:
  - "AM Pharma" != "AM-Pharma"
  - "Colisee" != "Colisée"


