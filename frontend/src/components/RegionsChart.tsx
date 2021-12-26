import {
  Chart as ChartJS, CategoryScale, LinearScale,
  BarElement, Title, Tooltip } from 'chart.js'
import { Bar } from 'react-chartjs-2'
import type { Regions } from '../types'

interface Props {
  regions: Regions,
}

ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip)
ChartJS.defaults.font.size = 14
ChartJS.defaults.font.family = `'Fira Sans', sans-serif`

const options = {
  indexAxis: 'y' as const,
  responsive: true,
  maintainAspectRatio: false,
  elements: {
    bar: {
      base: 10
    }
  },
  plugins: {
    title: {
      display: true,
      text: 'Accuracy by region',
      font: {
        size: 20,
      },
    },
    tooltip: {
      callbacks: {
        label: (ctx: any) => {
          return ` ${ctx.dataset.label}: ${ctx.parsed.x}%`
        }
      }
    },
  },
}

const calculateColour = (vals: number[]) => {
  const high = [84, 130, 53]
  const low = [198, 224, 180]
  const steps = 20
  const delta = (c1: number, c2: number, step: number) => (
    c1 - (c1 - c2) * (step / steps)
  )
  const colours = Array(steps).fill(null).map(
    (_, i) => `rgb(${Array(3).fill(null).map(
      (_, j) => delta(low[j], high[j], i)).join(`, `)
    })`
  )
  const getColourIndex = (v: number) => {
    if(v === Math.max(...vals)){
      return (steps - 1)
    } else if (v === Math.min(...vals)){
      return 0
    }
    return Math.floor(
      (v - Math.min(...vals)) / 
      ((Math.max(...vals) - Math.min(...vals)) / steps)
    )
  }
  return vals.map((v) => colours[getColourIndex(v)])
  
}

const RegionsChart = ({ regions }: Props) => {
  const arr = Object.entries(regions).sort(([n1], [n2]) => n1.localeCompare(n2))
  const labels = arr.map(([l]) => l)
  const accuracyData = arr.map(([_, { accuracy }]) => (100 * accuracy).toFixed(2))
  const datasets = [
    {
      label: `Accuracy`,
      data: accuracyData,
      borderColor: 'rgb(20, 20, 20)',
      backgroundColor: calculateColour(accuracyData.map((v) => Number.parseFloat(v))),
    },
  ]
  const chartData = {
    labels,
    datasets,
  }

  return <Bar options={options} data={chartData} />
}

export default RegionsChart