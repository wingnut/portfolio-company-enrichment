# EQT Code Test - Michael Mrazek

### Next Steps
- [ ] Download all files in parallel in main() before creating and running pipeline

- [ ] Try running with real full dataset to spot any outliers

- [ ] Download "divestment" companies too and concatenate with portfolio companies
  - Use a flag for divestment or separate record?
  - NOTE: one extra field: exitDate

- [ ] Download compressed funds reference data and use as enrichment for funds list in portfolio company
  - Also enrich with stuff from each individual fund page??? Seems like overkill.
    - The resulting data set would blow up in size due to the long description of the fund
    - DirectRunners (local dev) don't support downloading over http, so it would get messy in local dev

## Next logical steps for a production deploy
- Deploy and run with a "production" runner instead of DirectRunner (means we can access files directly over http), Spark, Flink, Google Cloud Dataflow etc 
- Upload resulting file to a GCP bucket
- Dockerize the app
- Set up infrastructure to run using AirFlow?