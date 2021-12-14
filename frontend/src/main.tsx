// import 'vite/modulepreload-polyfill'
import { render } from 'preact'
import App from './app'
import './index.scss'

render(<App />, document.getElementById('app')!)
