import {randomString} from 'aster-common'
import {signal, type Signal} from '@preact/signals'

export enum AlertType {
    Info = "info",
    Warn = "warn",
    Error = "error",
    Success = "success",
}

export class Alert {
    public id: string = randomString()
    public title?: string = ""
    public type: AlertType = AlertType.Info
    public message: string = ""

    constructor(title: string, type: AlertType, message: string) {
        this.title = title
        this.type = type
        this.message = message
    }
}


class AlertManager {
    public state: Signal<Alert[]> = signal([])

    public add(alert: Alert) {
        this.state.value = this.state.value.concat(alert)

        setTimeout(() => {
            this.state.value = this.state.value.filter((e) => e.id !== alert.id)
        }, 3000)
    }
}

export default new AlertManager()
