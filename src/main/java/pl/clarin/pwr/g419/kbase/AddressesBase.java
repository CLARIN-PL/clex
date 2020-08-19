package pl.clarin.pwr.g419.kbase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AddressesBase {

  private static final String CITY = "address__city";
  private static final String POSTAL_CODE = "address__postal_code";
  private static final String STREET = "address__street";
  private static final String STREET_NO = "address__street_no";

  String resourcePath = "/kb-addresses.yml";
  Map<String, HashMap<String, String>> addresses = Maps.newHashMap();

  public AddressesBase() {
    final InputStream stream = AddressesBase.class.getResourceAsStream(resourcePath);
    final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    try {
      this.addresses.putAll(mapper.readValue(stream, HashMap.class));
    } catch (final IOException e) {
      log.error("Failed to load database of addresses", e);
    }
  }

  public Optional<String> getCity(final String company) {
    return getValue(company, CITY);
  }

  public Optional<String> getPostalCode(final String company) {
    return getValue(company, POSTAL_CODE);
  }

  public Optional<String> getStreet(final String company) {
    return getValue(company, STREET);
  }

  public Optional<String> getStreetNo(final String company) {
    return getValue(company, STREET_NO);
  }

  private Optional<String> getValue(final String company, final String fieldName) {
    if (addresses.containsKey(company) && addresses.get(company).containsKey(fieldName)) {
      return Optional.of(addresses.get(company).get(fieldName));
    } else {
      return Optional.empty();
    }
  }
}
