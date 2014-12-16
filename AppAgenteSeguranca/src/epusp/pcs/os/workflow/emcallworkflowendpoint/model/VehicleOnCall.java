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
 * on 2014-12-16 at 05:06:36 UTC 
 * Modify at your own risk.
 */

package epusp.pcs.os.workflow.emcallworkflowendpoint.model;

/**
 * Model definition for VehicleOnCall.
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
public final class VehicleOnCall extends com.google.api.client.json.GenericJson {

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.util.List<java.lang.String> agents;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String id;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private Position lastPosition;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.util.List<Position> positions;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Integer size;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String vehicleIdTag;

  /**
   * @return value or {@code null} for none
   */
  public java.util.List<java.lang.String> getAgents() {
    return agents;
  }

  /**
   * @param agents agents or {@code null} for none
   */
  public VehicleOnCall setAgents(java.util.List<java.lang.String> agents) {
    this.agents = agents;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getId() {
    return id;
  }

  /**
   * @param id id or {@code null} for none
   */
  public VehicleOnCall setId(java.lang.String id) {
    this.id = id;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public Position getLastPosition() {
    return lastPosition;
  }

  /**
   * @param lastPosition lastPosition or {@code null} for none
   */
  public VehicleOnCall setLastPosition(Position lastPosition) {
    this.lastPosition = lastPosition;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.util.List<Position> getPositions() {
    return positions;
  }

  /**
   * @param positions positions or {@code null} for none
   */
  public VehicleOnCall setPositions(java.util.List<Position> positions) {
    this.positions = positions;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Integer getSize() {
    return size;
  }

  /**
   * @param size size or {@code null} for none
   */
  public VehicleOnCall setSize(java.lang.Integer size) {
    this.size = size;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getVehicleIdTag() {
    return vehicleIdTag;
  }

  /**
   * @param vehicleIdTag vehicleIdTag or {@code null} for none
   */
  public VehicleOnCall setVehicleIdTag(java.lang.String vehicleIdTag) {
    this.vehicleIdTag = vehicleIdTag;
    return this;
  }

  @Override
  public VehicleOnCall set(String fieldName, Object value) {
    return (VehicleOnCall) super.set(fieldName, value);
  }

  @Override
  public VehicleOnCall clone() {
    return (VehicleOnCall) super.clone();
  }

}
