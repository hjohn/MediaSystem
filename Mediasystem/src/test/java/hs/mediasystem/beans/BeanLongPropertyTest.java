package hs.mediasystem.beans;

import javafx.beans.property.SimpleLongProperty;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BeanLongPropertyTest {
  private BeanLongProperty property;
  private TestBean testBean;

  @Before
  public void before() {
    property = new BeanLongProperty(new BeanAccessor<Long>(this, "testBean", "value"));
    testBean = new TestBean();
  }

  public TestBean getTestBean() {
    return testBean;
  }

  @Test
  public void shouldReturnInitialValue() {
    Assert.assertEquals(5, property.get());
  }

  @Test
  public void shouldNoticeUpdate() {
    SimpleLongProperty binded = new SimpleLongProperty(2);

    binded.bind(property);

    Assert.assertEquals(5, binded.get());

    testBean.setValueSecretly(1);

    Assert.assertEquals(5, binded.get());

    property.update();

    Assert.assertEquals(1, binded.get());

    testBean.setValueSecretly(2);
    property.update();

    Assert.assertEquals(2, binded.get());
  }

  @Test
  public void shouldHandleNull() {
    testBean = null;

    Assert.assertEquals(0, property.get());
  }

  public static class TestBean {
    private long value = 5;

    public long getValue() {
      return value;
    }

    public void setValueSecretly(long value) {
      this.value = value;
    }
  }
}
