export type Region = { lat: number, lng: number, accuracy: number }
export type Regions = {
  [name: string]: Region
}

export interface Data {
  forecasts_count: number,
  overall_accuracy: number,
  mistakes_count: number,
  period: string
  regions: Regions,
}