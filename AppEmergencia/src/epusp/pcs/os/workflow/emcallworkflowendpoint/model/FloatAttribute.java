/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
/*
 * This code was generated by https://code.google.com/p/google-apis-client-generator/
 * (build: 2014-11-17 18:43:33 UTC)
 * on 2014-12-14 at 11:57:32 UTC 
 * Modify at your own risk.
 */

package epusp.pcs.os.workflow.emcallworkflowendpoint.model;

/**
 * Model definition for FloatAttribute.
 *
 * <p> This is the Java data model class that specifies how to parse/serialize into the JSON that is
 * transmitted over HTTP when working with the emcallworkflowendpoint. For a detailed explanation
 * see:
 * <a href="http://code.google.com/p/google-http-java-client/wiki/JSON">http://code.google.com/p/google-http-java-client/wiki/JSON</a>
 * </p>
 *
 * @author Google, Inc.
 */
@SuppressWarnings("javadoc")
public final class FloatAttribute extends com.google.api.client.json.GenericJson {

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String attributeName;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String dataType;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Float value;

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getAttributeName() {
    return attributeName;
  }

  /**
   * @param attributeName attributeName or {@code null} for none
   */
  public FloatAttribute setAttributeName(java.lang.String attributeName) {
    this.attributeName = attributeName;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getDataType() {
    return dataType;
  }

  /**
   * @param dataType dataType or {@code null} for none
   */
  public FloatAttribute setDataType(java.lang.String dataType) {
    this.dataType = dataType;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Float getValue() {
    return value;
  }

  /**
   * @param value value or {@code null} for none
   */
  public FloatAttribute setValue(java.lang.Float value) {
    this.value = value;
    return this;
  }

  @Override
  public FloatAttribute set(String fieldName, Object value) {
    return (FloatAttribute) super.set(fieldName, value);
  }

  @Override
  public FloatAttribute clone() {
    return (FloatAttribute) super.clone();
  }

}
