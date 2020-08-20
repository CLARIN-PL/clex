package pl.clarin.pwr.g419.kbase

import pl.clarin.pwr.g419.kbase.AddressesBase
import spock.lang.Specification
import spock.lang.Subject


class AddressesBaseTest extends Specification {

    @Subject
    def addresses = new AddressesBase()

    def "constructor should load default database of addresses"() {
        expect:
            addresses.addresses.size() > 0
    }

    def "getCity should return valid result for existing company name"() {
        when:
            def city = addresses.getCity("MUZA")

        then:
            city.isPresent()

        and:
            city.get() == "Warszawa"
    }

    def "getCity should return valid result for non-existing company name"() {
        when:
            def city = addresses.getCity("UNKNOWN")

        then:
            city.isEmpty()
    }
}
