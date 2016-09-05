package eu.toolchain.datalock;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class Protobuf {
  public static class PartitionIdToProto
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

  public static class PartitionIdFromProto
      implements FromProto<PartitionId, com.google.datastore.v1.PartitionId> {
    @Override
    public PartitionId apply(final com.google.datastore.v1.PartitionId pb) {
      final String namespaceId = pb.getNamespaceId();
      final String projectId = pb.getProjectId();
      return new PartitionId(namespaceId, projectId);
    }
  }

  public static class PathElementToProto
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

  public static class PathElementFromProto
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

  public static class KeyToProto implements ToProto<Key, com.google.datastore.v1.Key> {
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

  public static class KeyFromProto implements FromProto<Key, com.google.datastore.v1.Key> {
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

  public static class ValueToProto implements ToProto<Value, com.google.datastore.v1.Value> {
    @Override
    public com.google.datastore.v1.Value apply(final Value instance) {
      return null;
    }
  }

  public static class ValueFromProto implements FromProto<Value, com.google.datastore.v1.Value> {
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
          return new Value.BlobValue(pb.getBlobValue(), excludeFromIndexes);
        case TIMESTAMP_VALUE:
          return Value.TimestampValue.fromPb(pb.getTimestampValue(), excludeFromIndexes);
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

  public static class EntityToProto
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

  public static class EntityFromProto implements FromProto<Entity, com.google.datastore.v1.Entity> {
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

  public static class MutationToProto
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

  public final FromProto<MutationResult, com.google.datastore.v1.MutationResult>
      mutationResultFromProto = pb -> {
    final Optional<Key> key =
        pb.hasKey() ? Optional.of(keyFromProto.apply(pb.getKey())) : Optional.empty();
    return new MutationResult(key);
  };

  /* request/response */

  public final ToProto<LookupRequest, com.google.datastore.v1.LookupRequest> lookupRequestToProto =
      instance -> {
        final com.google.datastore.v1.LookupRequest.Builder builder =
            com.google.datastore.v1.LookupRequest.newBuilder();
        builder.setProjectId(projectId);
        instance.getKeys().stream().map(keyToProto).forEach(builder::addKeys);
        instance
            .getReadOptions()
            .ifPresent(r -> builder.setReadOptions(readOptionsToProto.apply(r)));
        return builder.build();
      };

  public final ToProto<AllocateIdsRequest, com.google.datastore.v1.AllocateIdsRequest>
      allocateIdsRequestToProto = instance -> {
    final com.google.datastore.v1.AllocateIdsRequest.Builder builder =
        com.google.datastore.v1.AllocateIdsRequest.newBuilder();
    builder.setProjectId(projectId);
    instance.getKeys().forEach(k -> builder.addKeys(keyToProto.apply(k)));
    return builder.build();
  };

  public final FromProto<AllocateIdsResponse, com.google.datastore.v1.AllocateIdsResponse>
      allocateIdsResponseFromProto = pb -> {
    final List<Key> keys = pb.getKeysList().stream().map(keyFromProto).collect(Collectors.toList());
    return new AllocateIdsResponse(keys);
  };

  public final ToProto<BeginTransactionRequest, com.google.datastore.v1.BeginTransactionRequest>
      beginTransactionRequestToProto = instance -> {
    final com.google.datastore.v1.BeginTransactionRequest.Builder builder =
        com.google.datastore.v1.BeginTransactionRequest.newBuilder();
    builder.setProjectId(projectId);
    return builder.build();
  };

  public final FromProto<BeginTransactionResponse, com.google.datastore.v1.BeginTransactionResponse>
      beginTransactionResponseFromProto = pb -> {
    return new BeginTransactionResponse(new Transaction(pb.getTransaction()));
  };

  public final ToProto<CommitRequest, com.google.datastore.v1.CommitRequest> commitRequestToProto =
      new ToProto<CommitRequest, com.google.datastore.v1.CommitRequest>() {
        public final ToProto<CommitRequest.Mode, com.google.datastore.v1.CommitRequest.Mode>
            modeToProto = instance -> {
          switch (instance) {
            case NON_TRANSACTIONAL:
              return com.google.datastore.v1.CommitRequest.Mode.NON_TRANSACTIONAL;
            case TRANSACTIONAL:
              return com.google.datastore.v1.CommitRequest.Mode.TRANSACTIONAL;
            default:
              throw new IllegalArgumentException("Unsupported mode: " + instance);
          }
        };

        @Override
        public com.google.datastore.v1.CommitRequest apply(
            final CommitRequest instance
        ) {
          final com.google.datastore.v1.CommitRequest.Builder builder =
              com.google.datastore.v1.CommitRequest.newBuilder();
          builder.setProjectId(projectId);
          instance.getMutations().stream().map(mutationToProto).forEach(builder::addMutations);
          instance.getTransaction().ifPresent(t -> builder.setTransaction(t.getBytes()));
          builder.setMode(modeToProto.apply(instance.getMode()));
          return builder.build();
        }
      };

  public final FromProto<CommitResponse, com.google.datastore.v1.CommitResponse>
      commitResponseFromProto = pb -> {
    final List<MutationResult> mutationResult = pb
        .getMutationResultsList()
        .stream()
        .map(mutationResultFromProto)
        .collect(Collectors.toList());

    return new CommitResponse(mutationResult);
  };

  @FunctionalInterface
  interface ToProto<T, P> extends Function<T, P> {
  }

  @FunctionalInterface
  interface FromProto<T, P> extends Function<P, T> {
  }
}
