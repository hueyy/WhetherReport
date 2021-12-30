import type { Data, Region } from './types'
import RegionsChart from './components/RegionsChart'
import AccuracyChart from './components/AccuracyChart'

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
  accuracy,
  mistakes_count: mistakesCount,
  period,
  regions,
  weekly_accuracy: weeklyAccuracy
} = data

const regionArr = Object.entries(regions).sort(
  ([_1, n1], [_2, n2]) => (n1.accuracy - n2.accuracy)
)

const formatNumber = (n: number) => n.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",")

const App = () => {
  return (
    <>
    <div className="main">
      <h1>WhetherReport</h1>

      <div className="prose-container">
        <p className="prose">
          Between <em>{period}</em>, <a href="http://www.weather.gov.sg/weather-forecast-2hrnowcast-2/">Meteorological Service Singapore (MSS)</a> made a total of <em>{formatNumber(forecastsCount)}</em> 2-hour weather forecasts. <em>{formatNumber(mistakesCount)}</em> of them turned out to be incorrect, resulting in an overall accuracy rate of <em>{round(accuracy.overall * 100)}%</em>.
        </p>

        <p className="prose">
          The 2-hour forecasts were most accurate in relation to <em>{regionArr[regionArr.length - 1][0]} ({round(regionArr[regionArr.length - 1][1].accuracy * 100)}%)</em> and least accurate in relation to <em>{regionArr[0][0]} ({round(regionArr[0][1].accuracy * 100)}%)</em>.
        </p>

        <p className="prose">
          It seems that MSS may have&nbsp;
          <a href="https://en.wikipedia.org/wiki/Wet_bias">'wet bias'</a>, i.e. the 2-hour forecasts may be more accurate generally where they predict non-rainy weather.
          MSS's predictions that the weather would be non-rainy are accurate <em>{round(accuracy.non_rain * 100)}%</em> of the time.
          However, where MSS predicted there would be rainy weather, it only actually rained <em>{round(accuracy.rain * 100)}%</em> of the time.
        </p>
      </div>

      <div className="accuracy-chart">
        <AccuracyChart weeklyAccuracy={weeklyAccuracy} />
      </div>

      <div className="regions-chart">
        <RegionsChart regions={regions} />
      </div>
      
      <div className="description">
        <p>
          This project tracks the accuracy of the 2-hour weather forecasts made by MSS (also available on&nbsp;
          <a href="https://va.ecitizen.gov.sg/CFP/CustomerPages/NEA_google/displayresult.aspx?MesId=3725718&Source=Google&url=va.ecitizen.gov.sg">NEA's myENV mobile app</a>
          ) by comparing rainfall recorded by the National Environment Agency's weather stations against the 2-hour forecasts made by MSS for the same time period. Please note the following:
        </p>
        <ul>
          <li>Only MSS's 2-hour weather forecasts are currently being analysed</li>
          <li>MSS's historical forecast data and NEA's historical weather conditions data are not fully comprehensive, and any missing or invalid data is ignored</li>
          <li>Since MSS's forecasts do not necessarily cover the same location where  NEA's weather stations are located, data from the closest few stations are used. It is therefore possible, e.g. that rainfall predicted by MSS is not recorded by NEA's weather station</li>
          <li>Weather forecasts are assessed leniently, i.e. if MSS forecasts rain in any form (e.g. Light Showers, Drizzle, Heavy Rain, Thundery Showers, etc.), and there is any precipitation at all within the 2-hour period covered by that forecast, it is considered to be correct (and vice-versa for non-rain forecasts, e.g. Windy, Fair, Cloudy, etc.)</li>
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