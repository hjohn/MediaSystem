package hs.mediasystem.ext.subtitle.sublight;

import hs.mediasystem.framework.SubtitleCriteriaProvider;
import hs.mediasystem.framework.SubtitleProvider;
import hs.mediasystem.framework.SubtitleProviderException;
import hs.mediasystem.util.CryptoUtil;
import hs.subtitle.SubtitleDescriptor;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Named;

@Named
public class SublightSubtitleProvider implements SubtitleProvider {
  private final SublightSubtitleClient client;

  public SublightSubtitleProvider() {
    String clientIdentity = CryptoUtil.decrypt("EBC4D196C84FF9CB52654303EDB969283377011F640F89D3D1F8527F540CF940", "-MediaSystem-");
    String apiKey = CryptoUtil.decrypt("F75D6CE82F7EBFADF37EF4956905386C99F344CB5E6B592AC8858111AE721DBDFB2E30CF7DF05228C16BDDAB9ABE1F63", "-MediaSystem-");

    client = new SublightSubtitleClient(clientIdentity, apiKey);
  }

  @Override
  public List<SubtitleDescriptor> query(Map<String, Object> criteria) throws SubtitleProviderException {
    System.out.println("[FINE] SublightSubtitleProvider.query() - Looking for subtitles: " + criteria);

    try {
      return client.getSubtitleList(
        (String)criteria.get(SubtitleCriteriaProvider.TITLE),
        (Integer)criteria.get(SubtitleCriteriaProvider.YEAR),
        (Integer)criteria.get(SubtitleCriteriaProvider.SEASON),
        (Integer)criteria.get(SubtitleCriteriaProvider.EPISODE),
        "English"
      );
    }
    catch(RuntimeException e) {
      throw new SubtitleProviderException(e.getMessage(), e);
    }
  }

  @Override
  public String getName() {
    return "Sublight";
  }

  @Override
  public Set<String> getMediaTypes() {
    return new HashSet<String>() {{
      add("movie");
      add("episode");
    }};
  }
}
