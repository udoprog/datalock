package eu.toolchain.datalock;

import com.google.datastore.v1.EntityResult;
import com.google.datastore.v1.PropertyFilter;
import com.google.protobuf.ByteString;
import com.google.protobuf.Int32Value;
import com.google.protobuf.NullValue;
import com.google.protobuf.Timestamp;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface Protobuf {
  class PartitionIdToProto
      implements ToProto<PartitionId, com.google.datastore.v1.PartitionId> {
    @Override
    public com.google.datastore.v1.PartitionId apply(final PartitionId instance) {
      final com.google.datastore.v1.PartitionId.Builder builder =
          com.google.datastore.v1.PartitionId.newBuilder();
      builder.setNamespaceId(instance.getNamespaceId());
      builder.setProjectId(instance.getProjectId());
      return builder.build();
    }
  }

  class PartitionIdFromProto
      implements FromProto<PartitionId, com.google.datastore.v1.PartitionId> {
    @Override
    public PartitionId apply(final com.google.datastore.v1.PartitionId pb) {
      final String namespaceId = pb.getNamespaceId();
      final String projectId = pb.getProjectId();
      return new PartitionId(namespaceId, projectId);
    }
  }

  class PathElementToProto
      implements PathElement.Visitor<com.google.datastore.v1.Key.PathElement>,
      ToProto<PathElement, com.google.datastore.v1.Key.PathElement> {
    @Override
    public com.google.datastore.v1.Key.PathElement apply(final PathElement instance) {
      return instance.visit(this);
    }

    @Override
    public com.google.datastore.v1.Key.PathElement visitIdPathElement(IdPathElement id) {
      return com.google.datastore.v1.Key.PathElement
          .newBuilder()
          .setKind(id.getKind())
          .setId(id.getId())
          .build();
    }

    @Override
    public com.google.datastore.v1.Key.PathElement visitNamePathElement(
        final NamePathElement name
    ) {
      return com.google.datastore.v1.Key.PathElement
          .newBuilder()
          .setKind(name.getKind())
          .setName(name.getName())
          .build();
    }

    @Override
    public com.google.datastore.v1.Key.PathElement visitIncompletePathElement(
        final IncompletePathElement incomplete
    ) {
      return com.google.datastore.v1.Key.PathElement
          .newBuilder()
          .setKind(incomplete.getKind())
          .build();
    }
  }

  class PathElementFromProto
      implements FromProto<PathElement, com.google.datastore.v1.Key.PathElement> {
    @Override
    public PathElement apply(final com.google.datastore.v1.Key.PathElement pb) {
      final String kind = pb.getKind();

      switch (pb.getIdTypeCase()) {
        case ID:
          return new IdPathElement(kind, pb.getId());
        case NAME:
          return new NamePathElement(kind, pb.getName());
        default:
          return new IncompletePathElement(kind);
      }
    }
  }

  class KeyToProto implements ToProto<Key, com.google.datastore.v1.Key> {
    private final PathElementToProto pathElementToProto = new PathElementToProto();
    private final PartitionIdToProto partitionIdToProto = new PartitionIdToProto();

    @Override
    public com.google.datastore.v1.Key apply(final Key instance) {
      final com.google.datastore.v1.Key.Builder builder = com.google.datastore.v1.Key.newBuilder();
      instance.getPath().stream().map(pathElementToProto).forEach(builder::addPath);
      builder.setPartitionId(partitionIdToProto.apply(instance.getPartitionId()));
      return builder.build();
    }
  }

  class KeyFromProto implements FromProto<Key, com.google.datastore.v1.Key> {
    private final PathElementFromProto pathElementFromProto = new PathElementFromProto();
    private final PartitionIdFromProto partitionIdFromProto = new PartitionIdFromProto();

    @Override
    public Key apply(final com.google.datastore.v1.Key pb) {
      final List<PathElement> elements = new ArrayList<>();
      pb.getPathList().stream().map(pathElementFromProto).forEach(elements::add);
      final PartitionId partitionId = partitionIdFromProto.apply(pb.getPartitionId());
      return new Key(elements, partitionId);
    }
  }

  class ValueToProto implements Value.Visitor<com.google.datastore.v1.Value>, ToProto<Value, com.google.datastore.v1.Value> {
    private final EntityToProto entityToProto = new EntityToProto();
    private final KeyToProto keyToProto = new KeyToProto();

    @Override
    public com.google.datastore.v1.Value apply(final Value instance) {
      return instance.visit(this);
    }

    @Override
    public com.google.datastore.v1.Value visitEntity(final Value.EntityValue instance) {
      return com.google.datastore.v1.Value
          .newBuilder()
          .setEntityValue(entityToProto.apply(instance.getEntity()))
          .setExcludeFromIndexes(instance.isExcludeFromIndexes())
          .build();
    }

    @Override
    public com.google.datastore.v1.Value visitString(final Value.StringValue instance) {
      return com.google.datastore.v1.Value
          .newBuilder()
          .setStringValue(instance.getValue())
          .setExcludeFromIndexes(instance.isExcludeFromIndexes())
          .build();
    }

    @Override
    public com.google.datastore.v1.Value visitKey(final Value.KeyValue instance) {
      return com.google.datastore.v1.Value
          .newBuilder()
          .setKeyValue(keyToProto.apply(instance.getValue()))
          .setExcludeFromIndexes(instance.isExcludeFromIndexes())
          .build();
    }

    @Override
    public com.google.datastore.v1.Value visitBlob(final Value.BlobValue instance) {
      final ByteString bytes = ByteString.copyFrom(instance.getValue());

      return com.google.datastore.v1.Value
          .newBuilder()
          .setBlobValue(bytes)
          .setExcludeFromIndexes(instance.isExcludeFromIndexes())
          .build();
    }

    @Override
    public com.google.datastore.v1.Value visitArray(final Value.ArrayValue instance) {
      final com.google.datastore.v1.Value.Builder valueBuilder =
          com.google.datastore.v1.Value.newBuilder();
      com.google.datastore.v1.ArrayValue.Builder array =
          com.google.datastore.v1.ArrayValue.newBuilder();
      instance.getValues().stream().map(this).forEach(array::addValues);

      return valueBuilder
          .setArrayValue(array.build())
          .setExcludeFromIndexes(instance.isExcludeFromIndexes())
          .build();
    }

    @Override
    public com.google.datastore.v1.Value visitInteger(final Value.IntegerValue instance) {
      return com.google.datastore.v1.Value.newBuilder().setIntegerValue(instance.getValue())
          .build();
    }

    @Override
    public com.google.datastore.v1.Value visitDouble(final Value.DoubleValue instance) {
      return com.google.datastore.v1.Value.newBuilder().setDoubleValue(instance.getValue()).build();
    }

    @Override
    public com.google.datastore.v1.Value visitBoolean(final Value.BooleanValue instance) {
      return com.google.datastore.v1.Value.newBuilder().setBooleanValue(instance.getValue())
          .build();
    }

    @Override
    public com.google.datastore.v1.Value visitTimestamp(final Value.TimestampValue instance) {
      final Timestamp.Builder ts = Timestamp.newBuilder();
      ts.setSeconds(instance.getSeconds());
      ts.setNanos(instance.getNanos());
      return com.google.datastore.v1.Value.newBuilder().setTimestampValue(ts.build()).build();
    }

    @Override
    public com.google.datastore.v1.Value visitNull(final Value.NullValue nothing) {
      return com.google.datastore.v1.Value.newBuilder().setNullValue(NullValue.NULL_VALUE).build();
    }

    @Override
    public com.google.datastore.v1.Value defaultAction(final Value value) {
      throw new IllegalArgumentException("Cannot be converted to protobuf: " + value);
    }
  }

  class ValueFromProto implements FromProto<Value, com.google.datastore.v1.Value> {
    private final KeyFromProto keyFromProto = new KeyFromProto();

    @Override
    public Value apply(final com.google.datastore.v1.Value pb) {
      final boolean excludeFromIndexes = pb.getExcludeFromIndexes();

      final com.google.datastore.v1.Value.ValueTypeCase c = pb.getValueTypeCase();

      switch (c) {
        case KEY_VALUE:
          return new Value.KeyValue(keyFromProto.apply(pb.getKeyValue()), excludeFromIndexes);
        case STRING_VALUE:
          return new Value.StringValue(pb.getStringValue(), excludeFromIndexes);
        case BLOB_VALUE:
          final ByteBuffer bytes = pb.getBlobValue().asReadOnlyByteBuffer();
          return new Value.BlobValue(bytes, excludeFromIndexes);
        case TIMESTAMP_VALUE:
          final Timestamp ts = pb.getTimestampValue();
          return new Value.TimestampValue(ts.getSeconds(), ts.getNanos(), excludeFromIndexes);
        case INTEGER_VALUE:
          return new Value.IntegerValue(pb.getIntegerValue(), excludeFromIndexes);
        case DOUBLE_VALUE:
          return new Value.DoubleValue(pb.getDoubleValue(), excludeFromIndexes);
        case BOOLEAN_VALUE:
          return new Value.BooleanValue(pb.getBooleanValue(), excludeFromIndexes);
        case ARRAY_VALUE:
          return new Value.ArrayValue(
              pb.getArrayValue().getValuesList().stream().map(this).collect(Collectors.toList()));
        case NULL_VALUE:
          return Value.NullValue.INSTANCE;
        default:
          throw new IllegalArgumentException("Unsupported case: " + c);
      }
    }
  }

  class EntityToProto
      implements Entity.Visitor<com.google.datastore.v1.Entity>, ToProto<Entity, com.google
      .datastore.v1.Entity> {
    private final KeyToProto keyToProto = new KeyToProto();
    private final ValueToProto valueToProto = new ValueToProto();

    @Override
    public com.google.datastore.v1.Entity apply(final Entity entity) {
      return entity.visit(this);
    }

    @Override
    public com.google.datastore.v1.Entity visitEmbedded(
        final Entity.EmbeddedEntity embedded
    ) {
      final com.google.datastore.v1.Entity.Builder builder =
          com.google.datastore.v1.Entity.newBuilder();

      for (final Map.Entry<String, Value> e : embedded.getProperties().entrySet()) {
        builder.putProperties(e.getKey(), valueToProto.apply(e.getValue()));
      }

      return builder.build();
    }

    @Override
    public com.google.datastore.v1.Entity visitKeyed(
        final Entity.KeyedEntity keyed
    ) {
      final com.google.datastore.v1.Entity.Builder builder =
          com.google.datastore.v1.Entity.newBuilder();

      builder.setKey(keyToProto.apply(keyed.getKey()));

      for (final Map.Entry<String, Value> e : keyed.getProperties().entrySet()) {
        builder.putProperties(e.getKey(), valueToProto.apply(e.getValue()));
      }

      return builder.build();
    }
  }

  class QueryToProto implements ToProto<Query, com.google.datastore.v1.Query> {
    private final PropertyOrderToProto propertyOrderToProto = new PropertyOrderToProto();
    private final FilterToProto filterToProto = new FilterToProto();

    @Override
    public com.google.datastore.v1.Query apply(final Query instance) {
      final com.google.datastore.v1.Query.Builder builder =
          com.google.datastore.v1.Query.newBuilder();

      instance.getFilter().ifPresent(f -> builder.setFilter(filterToProto.apply(f)));

      instance.getDistinctOn()
          .stream()
          .map(g -> com.google.datastore.v1.PropertyReference.newBuilder().setName(g).build())
          .forEach(builder::addDistinctOn);

      instance.getOrder().stream().map(propertyOrderToProto).forEach(builder::addOrder);

      instance.getKind().ifPresent(k -> builder.addKind(
          com.google.datastore.v1.KindExpression.newBuilder().setName(k).build()));

      instance.getStartCursor().ifPresent(b -> builder.setStartCursor(ByteString.copyFrom(b)));
      instance.getEndCursor().ifPresent(b -> builder.setEndCursor(ByteString.copyFrom(b)));

      instance.getLimit().map(i -> Int32Value.newBuilder().setValue(i).build())
          .ifPresent(builder::setLimit);

      return builder.build();
    }
  }

  class OperatorToProto implements ToProto<Filter.PropertyFilter.Operator, com.google.datastore.v1.PropertyFilter.Operator> {
    @Override
    public PropertyFilter.Operator apply(final Filter.PropertyFilter.Operator instance) {
      switch (instance) {
        case EQUAL:
          return com.google.datastore.v1.PropertyFilter.Operator.EQUAL;
        case GREATER_THAN:
          return com.google.datastore.v1.PropertyFilter.Operator.GREATER_THAN;
        case GREATER_THAN_OR_EQUAL:
          return com.google.datastore.v1.PropertyFilter.Operator.GREATER_THAN_OR_EQUAL;
        case LESS_THAN:
          return com.google.datastore.v1.PropertyFilter.Operator.LESS_THAN;
        case LESS_THAN_OR_EQUAL:
          return com.google.datastore.v1.PropertyFilter.Operator.LESS_THAN_OR_EQUAL;
        case HAS_ANCESTOR:
          return com.google.datastore.v1.PropertyFilter.Operator.HAS_ANCESTOR;
        default:
          throw new IllegalArgumentException("Unsupported operator: " + instance);
      }
    }
  }

  class FilterToProto implements Filter.Visitor<com.google.datastore.v1.Filter>, ToProto<Filter, com.google.datastore.v1.Filter> {
    private final ValueToProto valueToProto = new ValueToProto();
    private final OperatorToProto operatorToProto = new OperatorToProto();

    @Override
    public com.google.datastore.v1.Filter apply(final Filter instance) {
      return instance.visit(this);
    }

    @Override
    public com.google.datastore.v1.Filter visitComposite(final Filter.Composite instance) {
      final com.google.datastore.v1.CompositeFilter.Builder builder =
          com.google.datastore.v1.CompositeFilter.newBuilder();
      instance.getFilters().stream().map(this).forEach(builder::addFilters);
      builder.setOp(com.google.datastore.v1.CompositeFilter.Operator.AND);
      return com.google.datastore.v1.Filter
          .newBuilder()
          .setCompositeFilter(builder.build())
          .build();
    }

    @Override
    public com.google.datastore.v1.Filter visitPropertyFilter(final Filter.PropertyFilter instance) {
      final com.google.datastore.v1.PropertyFilter.Builder builder =
          com.google.datastore.v1.PropertyFilter.newBuilder();
      builder.setProperty(
          com.google.datastore.v1.PropertyReference.newBuilder().setName(instance.getProperty())
              .build());
      builder.setOp(operatorToProto.apply(instance.getOperator()));
      builder.setValue(valueToProto.apply(instance.getValue()));
      return com.google.datastore.v1.Filter.newBuilder().setPropertyFilter(builder.build()).build();
    }
  }

  class PropertyOrderToProto implements ToProto<PropertyOrder, com.google.datastore.v1.PropertyOrder> {
    private final DirectionToProto directionToProto = new DirectionToProto();

    @Override
    public com.google.datastore.v1.PropertyOrder apply(final PropertyOrder instance) {
      final com.google.datastore.v1.PropertyOrder.Builder builder =
          com.google.datastore.v1.PropertyOrder.newBuilder();
      builder.setProperty(
          com.google.datastore.v1.PropertyReference.newBuilder().setName(instance.getField())
              .build());
      builder.setDirection(directionToProto.apply(instance.getDirection()));
      return builder.build();
    }

    class DirectionToProto implements ToProto<PropertyOrder.Direction, com.google.datastore.v1.PropertyOrder.Direction> {
      @Override
      public com.google.datastore.v1.PropertyOrder.Direction apply(final PropertyOrder.Direction instance) {
        switch (instance) {
          case ASCENDING:
            return com.google.datastore.v1.PropertyOrder.Direction.ASCENDING;
          case DESCENDING:
            return com.google.datastore.v1.PropertyOrder.Direction.DESCENDING;
          default:
            throw new IllegalArgumentException("Unsupported direction: " + instance);
        }
      }
    }
  }

  public class EntityFromProto implements FromProto<Entity, com.google.datastore.v1.Entity> {
    private final KeyFromProto keyFromProto = new KeyFromProto();
    private final ValueFromProto valueFromProto = new ValueFromProto();

    @Override
    public Entity apply(final com.google.datastore.v1.Entity pb) {
      final Map<String, Value> properties = new HashMap<>();

      for (final Map.Entry<String, com.google.datastore.v1.Value> property : pb
          .getPropertiesMap()
          .entrySet()) {
        properties.put(property.getKey(), valueFromProto.apply(property.getValue()));
      }

      if (pb.hasKey()) {
        final Key key = keyFromProto.apply(pb.getKey());
        return new Entity.KeyedEntity(key, properties);
      } else {
        return new Entity.EmbeddedEntity(properties);
      }
    }
  }

  class KeyedEntityFromProto implements FromProto<Entity.KeyedEntity, com.google.datastore.v1.Entity> {
    private final KeyFromProto keyFromProto = new KeyFromProto();
    private final ValueFromProto valueFromProto = new ValueFromProto();

    @Override
    public Entity.KeyedEntity apply(final com.google.datastore.v1.Entity pb) {
      final Map<String, Value> properties = new HashMap<>();

      for (final Map.Entry<String, com.google.datastore.v1.Value> property : pb
          .getPropertiesMap()
          .entrySet()) {
        properties.put(property.getKey(), valueFromProto.apply(property.getValue()));
      }

      if (!pb.hasKey()) {
        throw new IllegalArgumentException("Expected key in entity");
      }

      final Key key = keyFromProto.apply(pb.getKey());
      return new Entity.KeyedEntity(key, properties);
    }
  }

  class MutationToProto
      implements Mutation.Visitor<com.google.datastore.v1.Mutation>, ToProto<Mutation, com.google
      .datastore.v1.Mutation> {
    private final KeyToProto keyToProto = new KeyToProto();
    private final EntityToProto entityToProto = new EntityToProto();

    @Override
    public com.google.datastore.v1.Mutation apply(final Mutation entity) {
      return entity.visit(this);
    }

    @Override
    public com.google.datastore.v1.Mutation visitDelete(
        final Mutation.DeleteMutation delete
    ) {
      final com.google.datastore.v1.Mutation.Builder builder =
          com.google.datastore.v1.Mutation.newBuilder();
      builder.setDelete(keyToProto.apply(delete.getKey()));
      return builder.build();
    }

    @Override
    public com.google.datastore.v1.Mutation visitInsert(
        final Mutation.InsertMutation insert
    ) {
      final com.google.datastore.v1.Mutation.Builder builder =
          com.google.datastore.v1.Mutation.newBuilder();
      builder.setInsert(entityToProto.apply(insert.getEntity()));
      return builder.build();
    }

    @Override
    public com.google.datastore.v1.Mutation visitUpsert(
        final Mutation.UpsertMutation upsert
    ) {
      final com.google.datastore.v1.Mutation.Builder builder =
          com.google.datastore.v1.Mutation.newBuilder();
      builder.setUpsert(entityToProto.apply(upsert.getEntity()));
      return builder.build();
    }

    @Override
    public com.google.datastore.v1.Mutation visitUpdate(
        final Mutation.UpdateMutation update
    ) {
      final com.google.datastore.v1.Mutation.Builder builder =
          com.google.datastore.v1.Mutation.newBuilder();
      builder.setUpdate(entityToProto.apply(update.getEntity()));
      return builder.build();
    }
  }

  class RunQueryResponseFromProto implements FromProto<RunQueryResponse, com.google.datastore.v1.RunQueryResponse> {
    private final KeyedEntityFromProto keyedEntityFromProto = new KeyedEntityFromProto();
    private final ResultTypeFromProto resultTypeFromProto = new ResultTypeFromProto();

    @Override
    public RunQueryResponse apply(final com.google.datastore.v1.RunQueryResponse pb) {
      final com.google.datastore.v1.QueryResultBatch batch = pb.getBatch();

      final List<Entity.KeyedEntity> entities = new ArrayList<>();

      batch
          .getEntityResultsList()
          .stream()
          .filter(com.google.datastore.v1.EntityResult::hasEntity)
          .map(e -> keyedEntityFromProto.apply(e.getEntity()))
          .forEach(entities::add);

      final Optional<ByteBuffer> cursor = Optional.ofNullable(batch.getEndCursor())
          .map(ByteString::asReadOnlyByteBuffer);

      final RunQueryResponse.ResultType resultType = resultTypeFromProto
          .apply(batch.getEntityResultType());
      return new RunQueryResponse(entities, cursor, resultType);
    }
  }

  class RunQueryRequestToProto implements ToProto<RunQueryRequest, com.google.datastore.v1.RunQueryRequest> {
    private final String projectId;

    private final QueryToProto queryToProto = new QueryToProto();
    private final ReadOptionsToProto readOptionsToProto = new ReadOptionsToProto();
    private final PartitionIdToProto partitionIdToProto = new PartitionIdToProto();

    public RunQueryRequestToProto(final String projectId) {
      this.projectId = projectId;
    }

    @Override
    public com.google.datastore.v1.RunQueryRequest apply(final RunQueryRequest instance) {
      final com.google.datastore.v1.RunQueryRequest.Builder builder =
          com.google.datastore.v1.RunQueryRequest.newBuilder();
      builder.setProjectId(projectId);
      builder.setQuery(queryToProto.apply(instance.getQuery()));
      builder.setReadOptions(readOptionsToProto.apply(instance.getReadOptions()));
      builder.setPartitionId(partitionIdToProto.apply(instance.getPartitionId()));
      return builder.build();
    }
  }

  class ResultTypeFromProto implements FromProto<RunQueryResponse.ResultType, com.google.datastore.v1.EntityResult.ResultType> {
    @Override
    public RunQueryResponse.ResultType apply(final EntityResult.ResultType pb) {
      switch (pb) {
        case FULL:
          return RunQueryResponse.ResultType.FULL;
        case KEY_ONLY:
          return RunQueryResponse.ResultType.KEY_ONLY;
        case PROJECTION:
          return RunQueryResponse.ResultType.PROJECTION;
        default:
          throw new IllegalArgumentException("Unsupported result type: " + pb);
      }
    }
  }

  class RollbackRequestToProto implements ToProto<RollbackRequest, com.google.datastore.v1.RollbackRequest> {
    @Override
    public com.google.datastore.v1.RollbackRequest apply(final RollbackRequest instance) {
      final com.google.datastore.v1.RollbackRequest.Builder builder =
          com.google.datastore.v1.RollbackRequest.newBuilder();
      final ByteString bytes = ByteString.copyFrom(instance.getTransaction().getBytes());
      builder.setTransaction(bytes);
      return builder.build();
    }
  }

  class LookupResponseFromProto implements FromProto<LookupResponse, com.google.datastore.v1.LookupResponse> {
    private final KeyFromProto keyFromProto = new KeyFromProto();
    private final KeyedEntityFromProto entityFromProto = new KeyedEntityFromProto();

    @Override
    public LookupResponse apply(final com.google.datastore.v1.LookupResponse pb) {
      final List<Key> deferred =
          pb.getDeferredList().stream().map(keyFromProto).collect(Collectors.toList());

      final List<Entity.KeyedEntity> found = pb
          .getFoundList()
          .stream()
          .filter(EntityResult::hasEntity)
          .map(EntityResult::getEntity)
          .map(entityFromProto)
          .collect(Collectors.toList());

      final List<Entity.KeyedEntity> missing = pb
          .getMissingList()
          .stream()
          .filter(EntityResult::hasEntity)
          .map(EntityResult::getEntity)
          .map(entityFromProto)
          .collect(Collectors.toList());

      return new LookupResponse(deferred, found, missing);
    }
  }

  class MutationResultFromProto implements FromProto<MutationResult, com.google.datastore.v1.MutationResult> {
    private final KeyFromProto keyFromProto = new KeyFromProto();

    @Override
    public MutationResult apply(final com.google.datastore.v1.MutationResult pb) {
      final Optional<Key> key =
          pb.hasKey() ? Optional.of(keyFromProto.apply(pb.getKey())) : Optional.empty();
      return new MutationResult(key);
    }
  }

  class ReadOptionsToProto implements ReadOptions.Visitor<com.google.datastore.v1.ReadOptions>, ToProto<ReadOptions, com.google.datastore.v1.ReadOptions> {
    private final KeyFromProto keyFromProto = new KeyFromProto();
    private final ReadConsistencyToProto readConsistencyToProto = new ReadConsistencyToProto();

    @Override
    public com.google.datastore.v1.ReadOptions apply(final ReadOptions instance) {
      return instance.visit(this);
    }

    @Override
    public com.google.datastore.v1.ReadOptions visitTransaction(final ReadOptions.TransactionReadOptions transaction) {
      final com.google.datastore.v1.ReadOptions.Builder builder =
          com.google.datastore.v1.ReadOptions.newBuilder();
      final ByteBuffer bytes = transaction.getTransaction().getBytes();
      builder.setTransaction(ByteString.copyFrom(bytes));
      return builder.build();
    }

    @Override
    public com.google.datastore.v1.ReadOptions visitConsistency(final ReadOptions.ConsistencyReadOptions instance) {
      final com.google.datastore.v1.ReadOptions.Builder builder =
          com.google.datastore.v1.ReadOptions.newBuilder();
      builder.setReadConsistency(readConsistencyToProto.apply(instance.getConsistency()));
      return builder.build();
    }

    class ReadConsistencyToProto implements ToProto<ReadOptions.ReadConsistency, com.google.datastore.v1.ReadOptions.ReadConsistency> {
      @Override
      public com.google.datastore.v1.ReadOptions.ReadConsistency apply(final ReadOptions.ReadConsistency instance) {
        switch (instance) {
          case STRONG:
            return com.google.datastore.v1.ReadOptions.ReadConsistency.STRONG;
          case EVENTUAL:
            return com.google.datastore.v1.ReadOptions.ReadConsistency.EVENTUAL;
          default:
            throw new IllegalArgumentException("Unsupported read consistency: " + instance);
        }
      }
    }
  }

  /* request/response */

  class LookupRequestToProto implements ToProto<LookupRequest, com.google.datastore.v1.LookupRequest> {
    private final String projectId;
    private final KeyToProto keyToProto;
    private final ReadOptionsToProto readOptionsToProto;

    public LookupRequestToProto(final String projectId) {
      this.projectId = projectId;
      this.keyToProto = new KeyToProto();
      this.readOptionsToProto = new ReadOptionsToProto();
    }

    @Override
    public com.google.datastore.v1.LookupRequest apply(final LookupRequest instance) {
      final com.google.datastore.v1.LookupRequest.Builder builder =
          com.google.datastore.v1.LookupRequest.newBuilder();
      builder.setProjectId(projectId);
      instance.getKeys().stream().map(keyToProto).forEach(builder::addKeys);
      builder.setReadOptions(readOptionsToProto.apply(instance.getReadOptions()));
      return builder.build();
    }
  }

  class AllocateIdsRequestToProto implements ToProto<AllocateIdsRequest, com.google.datastore.v1.AllocateIdsRequest> {
    private final String projectId;
    private final KeyToProto keyToProto;

    public AllocateIdsRequestToProto(final String projectId) {
      this.projectId = projectId;
      this.keyToProto = new KeyToProto();
    }

    @Override
    public com.google.datastore.v1.AllocateIdsRequest apply(final AllocateIdsRequest instance) {
      final com.google.datastore.v1.AllocateIdsRequest.Builder builder =
          com.google.datastore.v1.AllocateIdsRequest.newBuilder();
      builder.setProjectId(projectId);
      instance.getKeys().forEach(k -> builder.addKeys(keyToProto.apply(k)));
      return builder.build();
    }
  }

  class AllocateIdsResponseFromProto implements FromProto<AllocateIdsResponse, com.google.datastore.v1.AllocateIdsResponse> {
    private final KeyFromProto keyFromProto = new KeyFromProto();

    @Override
    public AllocateIdsResponse apply(final com.google.datastore.v1.AllocateIdsResponse pb) {
      final List<Key> keys = pb.getKeysList().stream().map(keyFromProto)
          .collect(Collectors.toList());
      return new AllocateIdsResponse(keys);
    }
  }

  class BeginTransactionRequestToProto implements ToProto<BeginTransactionRequest, com.google.datastore.v1.BeginTransactionRequest> {
    private final String projectId;

    public BeginTransactionRequestToProto(final String projectId) {
      this.projectId = projectId;
    }

    @Override
    public com.google.datastore.v1.BeginTransactionRequest apply(final BeginTransactionRequest instance) {
      final com.google.datastore.v1.BeginTransactionRequest.Builder builder =
          com.google.datastore.v1.BeginTransactionRequest.newBuilder();
      builder.setProjectId(projectId);
      return builder.build();
    }
  }

  class BeginTransactionResponseFromProto implements FromProto<BeginTransactionResponse, com.google.datastore.v1.BeginTransactionResponse> {
    @Override
    public BeginTransactionResponse apply(final com.google.datastore.v1.BeginTransactionResponse pb) {
      final ByteBuffer bytes = pb.getTransaction().asReadOnlyByteBuffer();
      return new BeginTransactionResponse(new Transaction(bytes));
    }
  }

  class CommitRequestToProto implements ToProto<CommitRequest, com.google.datastore.v1.CommitRequest> {
    private final String projectId;
    private final MutationToProto mutationToProto;
    private final ModeToProto modeToProto;

    public CommitRequestToProto(final String projectId) {
      this.projectId = projectId;
      this.mutationToProto = new MutationToProto();
      this.modeToProto = new ModeToProto();
    }

    @Override
    public com.google.datastore.v1.CommitRequest apply(final CommitRequest instance) {
      final com.google.datastore.v1.CommitRequest.Builder builder =
          com.google.datastore.v1.CommitRequest.newBuilder();
      builder.setProjectId(projectId);
      instance.getMutations().stream().map(mutationToProto).forEach(builder::addMutations);
      instance.getTransaction()
          .ifPresent(t -> builder.setTransaction(ByteString.copyFrom(t.getBytes())));
      builder.setMode(modeToProto.apply(instance.getMode()));
      return builder.build();
    }

    class ModeToProto implements ToProto<CommitRequest.Mode, com.google.datastore.v1.CommitRequest.Mode> {
      @Override
      public com.google.datastore.v1.CommitRequest.Mode apply(final CommitRequest.Mode instance) {
        switch (instance) {
          case NON_TRANSACTIONAL:
            return com.google.datastore.v1.CommitRequest.Mode.NON_TRANSACTIONAL;
          case TRANSACTIONAL:
            return com.google.datastore.v1.CommitRequest.Mode.TRANSACTIONAL;
          default:
            throw new IllegalArgumentException("Unsupported mode: " + instance);
        }
      }
    }
  }

  class CommitResponseFromProto implements FromProto<CommitResponse, com.google.datastore.v1.CommitResponse> {
    private final MutationResultFromProto mutationResultFromProto = new MutationResultFromProto();

    @Override
    public CommitResponse apply(final com.google.datastore.v1.CommitResponse pb) {
      final List<MutationResult> mutationResult = pb
          .getMutationResultsList()
          .stream()
          .map(mutationResultFromProto)
          .collect(Collectors.toList());

      return new CommitResponse(mutationResult);
    }
  }

  class RollbackResponseFromProto implements FromProto<RollbackResponse, com.google.datastore.v1.RollbackResponse> {
    @Override
    public RollbackResponse apply(final com.google.datastore.v1.RollbackResponse rollbackResponse) {
      return new RollbackResponse();
    }
  }

  @FunctionalInterface
  interface ToProto<T, P> extends Function<T, P> {
  }

  @FunctionalInterface
  interface FromProto<T, P> extends Function<P, T> {
  }
}
