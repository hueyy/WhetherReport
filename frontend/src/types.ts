export type Region = { lat: number, lng: number, accuracy: number }
export type Regions = {
  [name: string]: Region
}

export type WeeklyAccuracy = [string, number][]

export interface Data {
  forecasts_count: number,
  accuracy: {
    overall: number,
    rain: number,
    non_rain: number,
  },
  mistakes_count: number,
  period: string
  regions: Regions,
  weekly_accuracy: WeeklyAccuracy
}