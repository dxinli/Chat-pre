package showcase.material.muix

import muix.pickers.AdapterDateFns
import muix.pickers.DatePicker
import muix.pickers.LocalizationProvider
import react.FC
import react.Props

val DateAndTimePickersShowcase = FC<Props> {
    LocalizationProvider {
        dateAdapter = AdapterDateFns

        DatePicker()
    }
}
