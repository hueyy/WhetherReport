# Whether Report

> Tells you whether the weather forecast was right or not

This project tracks the accuracy of the Meteorological Service Singapore (MSS)'s weather forecasts by comparing rainfall recorded by the National Environment Agency (NEA)'s weather stations against the 2-hour forecasts made by MSS for the same time period.

## Limitations

Please note the following:

- Only MSS's 2-hour weather forecasts are currently being analysed
- MSS's historical forecast data and NEA's historical weather conditions data is not fully comprehensive, and any missing or invalid data is ignored
- Since MSS's forecasts do not necessarily cover the same location where  NEA's weather stations are located, data from the closest few stations are used. It is therefore possible, e.g. that rainfall predicted by MSS is not recorded by NEA's weather station.
- Weather forecasts are assessed leniently, i.e. if there is any precipitation at all within the 2-hour period covered by a forecast, it is considered to be correct.

## Development

Run the backend:

```bash
cd backend
lein ring server
```

Then run the frontend:

```bash
cd ../frontend
npm run dev
```

In Docker:

```bash
docker build . -t whether
docker run -p 8000:8000 whether
```

## Data

This project uses data from [data.gov.sg](https://data.gov.sg) that was made available under the terms of the [Singapore Open Data Licence v1.0](https://data.gov.sg/open-data-licence).
