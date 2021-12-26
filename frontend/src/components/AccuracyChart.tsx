import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip
} from 'chart.js'
import { Line } from 'react-chartjs-2'
import type { WeeklyAccuracy } from '../types'

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
)
ChartJS.defaults.font.size = 20
ChartJS.defaults.font.family = `'Fira Sans', sans-serif`

interface Props {
  weeklyAccuracy: WeeklyAccuracy,
}

const options = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    title: {
      display: true,
      text: `Accuracy across time`
    },
    tooltip: {
      callbacks: {
        label: (ctx: any) => {
          return ` ${ctx.dataset.label}: ${ctx.parsed.y}%`
        }
      }
    }
  }
}

const formatDate = (date: string) => {
  const d = new Date(date)
  const day = d.getDate()
  const month = ((new Intl.DateTimeFormat('en-US', {month:`short`})).format(d))
  return `${day} ${month}`
}

const AccuracyChart = ({ weeklyAccuracy }: Props) => {
  const datasets = [
    {
      label: `Accuracy`,
      data: weeklyAccuracy.map(([_, a] ) => (100 * a).toFixed(2)),
      borderColor: 'rgb(120, 120, 120)',
      backgroundColor: `rgb(20, 20, 20)`,
    },
    {
      label: `Weekly moving average`,
      data: weeklyAccuracy
        .map(([_, a], index) => {
          const total = weeklyAccuracy
            .slice(0, index + 1)
            .reduce((acc, [_, v]) => (acc + v), 0)
          return (100 * (total / (index + 1))).toFixed(2)
      }),
      borderColor: `rgb(124, 170, 93)`,
      backgroundColor: `rgb(84, 130, 53)`
    }
  ]
  const chartData = {
    labels: weeklyAccuracy.map(([t, _]) => formatDate(t)),
    datasets,
  }
  return <Line options={options} data={chartData} />
}

export default AccuracyChart