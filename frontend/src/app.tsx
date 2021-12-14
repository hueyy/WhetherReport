import type { Data, Region } from './types'
import RegionsChart from './components/RegionsChart'

declare const data: Data

const getMonthAbbr = (d: Date) => ((new Intl.DateTimeFormat('en-US', {month:`short`})).format(d))
const humanTime = (d: string) => {
  const date = new Date(d)
  const day = `${date.getDate()} ${getMonthAbbr(date)}`
  const time = `${String(date.getHours()).padStart(2, `0`)}:${String(date.getMinutes()).padStart(2, `0`)}hrs`
  return `${day}, ${time}`
}
const round = (n: number) => Math.round(n * 100) / 100

const {
  forecasts_count: forecastsCount,
  overall_accuracy: overallAccuracy,
  mistakes_count: mistakesCount,
  period,
  regions
} = data

const regionArr = Object.entries(regions).sort(
  ([_1, n1], [_2, n2]) => (n1.accuracy - n2.accuracy)
)

const App = () => {
  return (
    <>
    <div className="main">
      <h1>WhetherReport</h1>
      <p className="period">
        {period}
      </p>
      <div className="stats">
        <div className="stat">
          <p>{forecastsCount}</p>
          <p>Total Forecasts</p>
        </div>
        <div className="stat">
          <p>{mistakesCount}</p>
          <p>Incorrect Forecasts</p>
        </div>
        <div className="stat">
          <p>{round(overallAccuracy * 100)}%</p>
          <p>Overall Accuracy</p>
        </div>
        <div className="stat">
          <p>{round(regionArr[0][1].accuracy * 100)}%</p>
          <p>{regionArr[0][0]} (Lowest)</p>
        </div>
        <div className="stat">
          <p>{round(regionArr[regionArr.length - 1][1].accuracy * 100)}%</p>
          <p>{regionArr[regionArr.length - 1][0]} (Highest)</p>
        </div>
      </div>
      <div className="chart">
        <RegionsChart regions={regions} />
      </div>
      <div className="description">
        <p>This project tracks the accuracy of the&nbsp;
          <a href="http://www.weather.gov.sg/weather-forecast-2hrnowcast-2/">Meteorological Service Singapore (MSS)'s weather forecasts</a> (also available on&nbsp;
          <a href="https://va.ecitizen.gov.sg/CFP/CustomerPages/NEA_google/displayresult.aspx?MesId=3725718&Source=Google&url=va.ecitizen.gov.sg">NEA's myENV mobile app</a>
          ) by comparing rainfall recorded by the National Environment Agency's weather stations against the 2-hour forecasts made by MSS for the same time period. Please note the following:</p>
        <ul>
          <li>Only MSS's 2-hour weather forecasts are currently being analysed</li>
          <li>MSS's historical forecast data and NEA's historical weather conditions data is not fully comprehensive, and any missing or invalid data is ignored</li>
          <li>Since MSS's forecasts do not necessarily cover the same location where  NEA's weather stations are located, data from the closest few stations are used. It is therefore possible, e.g. that rainfall predicted by MSS is not recorded by NEA's weather station.</li>
          <li>Weather forecasts are assessed leniently, i.e. if there is any precipitation at all within the 2-hour period covered by a forecast, it is considered to be correct.</li>
        </ul>
        <p>
          The data used is obtained from&nbsp;
          <a href="https://data.gov.sg">data.gov.sg</a>
          &nbsp;and was made available under the terms of the&nbsp;
          <a href="https://data.gov.sg/open-data-licence">Singapore Open Data Licence v1.0</a></p>
      </div>
      <footer>
        built by&nbsp;
        <a href="https://huey.xyz">Huey</a>
        &nbsp;&nbsp;| &nbsp;&nbsp;
        <a href="https://github.com/hueyy/WhetherReport">source code</a>
      </footer>
    </div>
    </>
  )
}

export default App