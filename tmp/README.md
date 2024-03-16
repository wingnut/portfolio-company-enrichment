Intended for downloaded files when we run locally as the DirectRunner does not support fetching over http(s).
So as a separate step we first download datafiles (json or json.gz) needed from the web and then read them using TextIO.read().from("tmp/some-file").
With more advanced runners (when running on a more production-like env), this step is not needed.