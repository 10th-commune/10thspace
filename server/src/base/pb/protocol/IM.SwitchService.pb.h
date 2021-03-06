// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: IM.SwitchService.proto

#ifndef GOOGLE_PROTOBUF_INCLUDED_IM_2eSwitchService_2eproto
#define GOOGLE_PROTOBUF_INCLUDED_IM_2eSwitchService_2eproto

#include <limits>
#include <string>

#include <google/protobuf/port_def.inc>
#if PROTOBUF_VERSION < 3012000
#error This file was generated by a newer version of protoc which is
#error incompatible with your Protocol Buffer headers. Please update
#error your headers.
#endif
#if 3012003 < PROTOBUF_MIN_PROTOC_VERSION
#error This file was generated by an older version of protoc which is
#error incompatible with your Protocol Buffer headers. Please
#error regenerate this file with a newer version of protoc.
#endif

#include <google/protobuf/port_undef.inc>
#include <google/protobuf/io/coded_stream.h>
#include <google/protobuf/arena.h>
#include <google/protobuf/arenastring.h>
#include <google/protobuf/generated_message_table_driven.h>
#include <google/protobuf/generated_message_util.h>
#include <google/protobuf/inlined_string_field.h>
#include <google/protobuf/metadata_lite.h>
#include <google/protobuf/message_lite.h>
#include <google/protobuf/repeated_field.h>  // IWYU pragma: export
#include <google/protobuf/extension_set.h>  // IWYU pragma: export
// @@protoc_insertion_point(includes)
#include <google/protobuf/port_def.inc>
#define PROTOBUF_INTERNAL_EXPORT_IM_2eSwitchService_2eproto
PROTOBUF_NAMESPACE_OPEN
namespace internal {
class AnyMetadata;
}  // namespace internal
PROTOBUF_NAMESPACE_CLOSE

// Internal implementation detail -- do not use these members.
struct TableStruct_IM_2eSwitchService_2eproto {
  static const ::PROTOBUF_NAMESPACE_ID::internal::ParseTableField entries[]
    PROTOBUF_SECTION_VARIABLE(protodesc_cold);
  static const ::PROTOBUF_NAMESPACE_ID::internal::AuxiliaryParseTableField aux[]
    PROTOBUF_SECTION_VARIABLE(protodesc_cold);
  static const ::PROTOBUF_NAMESPACE_ID::internal::ParseTable schema[1]
    PROTOBUF_SECTION_VARIABLE(protodesc_cold);
  static const ::PROTOBUF_NAMESPACE_ID::internal::FieldMetadata field_metadata[];
  static const ::PROTOBUF_NAMESPACE_ID::internal::SerializationTable serialization_table[];
  static const ::PROTOBUF_NAMESPACE_ID::uint32 offsets[];
};
namespace IM {
namespace SwitchService {
class IMP2PCmdMsg;
class IMP2PCmdMsgDefaultTypeInternal;
extern IMP2PCmdMsgDefaultTypeInternal _IMP2PCmdMsg_default_instance_;
}  // namespace SwitchService
}  // namespace IM
PROTOBUF_NAMESPACE_OPEN
template<> ::IM::SwitchService::IMP2PCmdMsg* Arena::CreateMaybeMessage<::IM::SwitchService::IMP2PCmdMsg>(Arena*);
PROTOBUF_NAMESPACE_CLOSE
namespace IM {
namespace SwitchService {

// ===================================================================

class IMP2PCmdMsg PROTOBUF_FINAL :
    public ::PROTOBUF_NAMESPACE_ID::MessageLite /* @@protoc_insertion_point(class_definition:IM.SwitchService.IMP2PCmdMsg) */ {
 public:
  inline IMP2PCmdMsg() : IMP2PCmdMsg(nullptr) {};
  virtual ~IMP2PCmdMsg();

  IMP2PCmdMsg(const IMP2PCmdMsg& from);
  IMP2PCmdMsg(IMP2PCmdMsg&& from) noexcept
    : IMP2PCmdMsg() {
    *this = ::std::move(from);
  }

  inline IMP2PCmdMsg& operator=(const IMP2PCmdMsg& from) {
    CopyFrom(from);
    return *this;
  }
  inline IMP2PCmdMsg& operator=(IMP2PCmdMsg&& from) noexcept {
    if (GetArena() == from.GetArena()) {
      if (this != &from) InternalSwap(&from);
    } else {
      CopyFrom(from);
    }
    return *this;
  }

  static const IMP2PCmdMsg& default_instance();

  static void InitAsDefaultInstance();  // FOR INTERNAL USE ONLY
  static inline const IMP2PCmdMsg* internal_default_instance() {
    return reinterpret_cast<const IMP2PCmdMsg*>(
               &_IMP2PCmdMsg_default_instance_);
  }
  static constexpr int kIndexInFileMessages =
    0;

  friend void swap(IMP2PCmdMsg& a, IMP2PCmdMsg& b) {
    a.Swap(&b);
  }
  inline void Swap(IMP2PCmdMsg* other) {
    if (other == this) return;
    if (GetArena() == other->GetArena()) {
      InternalSwap(other);
    } else {
      ::PROTOBUF_NAMESPACE_ID::internal::GenericSwap(this, other);
    }
  }
  void UnsafeArenaSwap(IMP2PCmdMsg* other) {
    if (other == this) return;
    GOOGLE_DCHECK(GetArena() == other->GetArena());
    InternalSwap(other);
  }

  // implements Message ----------------------------------------------

  inline IMP2PCmdMsg* New() const final {
    return CreateMaybeMessage<IMP2PCmdMsg>(nullptr);
  }

  IMP2PCmdMsg* New(::PROTOBUF_NAMESPACE_ID::Arena* arena) const final {
    return CreateMaybeMessage<IMP2PCmdMsg>(arena);
  }
  void CheckTypeAndMergeFrom(const ::PROTOBUF_NAMESPACE_ID::MessageLite& from)
    final;
  void CopyFrom(const IMP2PCmdMsg& from);
  void MergeFrom(const IMP2PCmdMsg& from);
  PROTOBUF_ATTRIBUTE_REINITIALIZES void Clear() final;
  bool IsInitialized() const final;

  size_t ByteSizeLong() const final;
  const char* _InternalParse(const char* ptr, ::PROTOBUF_NAMESPACE_ID::internal::ParseContext* ctx) final;
  ::PROTOBUF_NAMESPACE_ID::uint8* _InternalSerialize(
      ::PROTOBUF_NAMESPACE_ID::uint8* target, ::PROTOBUF_NAMESPACE_ID::io::EpsCopyOutputStream* stream) const final;
  void DiscardUnknownFields();
  int GetCachedSize() const final { return _cached_size_.Get(); }

  private:
  inline void SharedCtor();
  inline void SharedDtor();
  void SetCachedSize(int size) const;
  void InternalSwap(IMP2PCmdMsg* other);
  friend class ::PROTOBUF_NAMESPACE_ID::internal::AnyMetadata;
  static ::PROTOBUF_NAMESPACE_ID::StringPiece FullMessageName() {
    return "IM.SwitchService.IMP2PCmdMsg";
  }
  protected:
  explicit IMP2PCmdMsg(::PROTOBUF_NAMESPACE_ID::Arena* arena);
  private:
  static void ArenaDtor(void* object);
  inline void RegisterArenaDtor(::PROTOBUF_NAMESPACE_ID::Arena* arena);
  public:

  std::string GetTypeName() const final;

  // nested types ----------------------------------------------------

  // accessors -------------------------------------------------------

  enum : int {
    kFromUserIdFieldNumber = 1,
    kToUserIdFieldNumber = 2,
    kCmdMsgDataFieldNumber = 3,
  };
  // string from_user_id = 1;
  void clear_from_user_id();
  const std::string& from_user_id() const;
  void set_from_user_id(const std::string& value);
  void set_from_user_id(std::string&& value);
  void set_from_user_id(const char* value);
  void set_from_user_id(const char* value, size_t size);
  std::string* mutable_from_user_id();
  std::string* release_from_user_id();
  void set_allocated_from_user_id(std::string* from_user_id);
  private:
  const std::string& _internal_from_user_id() const;
  void _internal_set_from_user_id(const std::string& value);
  std::string* _internal_mutable_from_user_id();
  public:

  // string to_user_id = 2;
  void clear_to_user_id();
  const std::string& to_user_id() const;
  void set_to_user_id(const std::string& value);
  void set_to_user_id(std::string&& value);
  void set_to_user_id(const char* value);
  void set_to_user_id(const char* value, size_t size);
  std::string* mutable_to_user_id();
  std::string* release_to_user_id();
  void set_allocated_to_user_id(std::string* to_user_id);
  private:
  const std::string& _internal_to_user_id() const;
  void _internal_set_to_user_id(const std::string& value);
  std::string* _internal_mutable_to_user_id();
  public:

  // string cmd_msg_data = 3;
  void clear_cmd_msg_data();
  const std::string& cmd_msg_data() const;
  void set_cmd_msg_data(const std::string& value);
  void set_cmd_msg_data(std::string&& value);
  void set_cmd_msg_data(const char* value);
  void set_cmd_msg_data(const char* value, size_t size);
  std::string* mutable_cmd_msg_data();
  std::string* release_cmd_msg_data();
  void set_allocated_cmd_msg_data(std::string* cmd_msg_data);
  private:
  const std::string& _internal_cmd_msg_data() const;
  void _internal_set_cmd_msg_data(const std::string& value);
  std::string* _internal_mutable_cmd_msg_data();
  public:

  // @@protoc_insertion_point(class_scope:IM.SwitchService.IMP2PCmdMsg)
 private:
  class _Internal;

  template <typename T> friend class ::PROTOBUF_NAMESPACE_ID::Arena::InternalHelper;
  typedef void InternalArenaConstructable_;
  typedef void DestructorSkippable_;
  ::PROTOBUF_NAMESPACE_ID::internal::ArenaStringPtr from_user_id_;
  ::PROTOBUF_NAMESPACE_ID::internal::ArenaStringPtr to_user_id_;
  ::PROTOBUF_NAMESPACE_ID::internal::ArenaStringPtr cmd_msg_data_;
  mutable ::PROTOBUF_NAMESPACE_ID::internal::CachedSize _cached_size_;
  friend struct ::TableStruct_IM_2eSwitchService_2eproto;
};
// ===================================================================


// ===================================================================

#ifdef __GNUC__
  #pragma GCC diagnostic push
  #pragma GCC diagnostic ignored "-Wstrict-aliasing"
#endif  // __GNUC__
// IMP2PCmdMsg

// string from_user_id = 1;
inline void IMP2PCmdMsg::clear_from_user_id() {
  from_user_id_.ClearToEmpty(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), GetArena());
}
inline const std::string& IMP2PCmdMsg::from_user_id() const {
  // @@protoc_insertion_point(field_get:IM.SwitchService.IMP2PCmdMsg.from_user_id)
  return _internal_from_user_id();
}
inline void IMP2PCmdMsg::set_from_user_id(const std::string& value) {
  _internal_set_from_user_id(value);
  // @@protoc_insertion_point(field_set:IM.SwitchService.IMP2PCmdMsg.from_user_id)
}
inline std::string* IMP2PCmdMsg::mutable_from_user_id() {
  // @@protoc_insertion_point(field_mutable:IM.SwitchService.IMP2PCmdMsg.from_user_id)
  return _internal_mutable_from_user_id();
}
inline const std::string& IMP2PCmdMsg::_internal_from_user_id() const {
  return from_user_id_.Get();
}
inline void IMP2PCmdMsg::_internal_set_from_user_id(const std::string& value) {
  
  from_user_id_.SetLite(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), value, GetArena());
}
inline void IMP2PCmdMsg::set_from_user_id(std::string&& value) {
  
  from_user_id_.SetLite(
    &::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), ::std::move(value), GetArena());
  // @@protoc_insertion_point(field_set_rvalue:IM.SwitchService.IMP2PCmdMsg.from_user_id)
}
inline void IMP2PCmdMsg::set_from_user_id(const char* value) {
  GOOGLE_DCHECK(value != nullptr);
  
  from_user_id_.SetLite(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), ::std::string(value),
              GetArena());
  // @@protoc_insertion_point(field_set_char:IM.SwitchService.IMP2PCmdMsg.from_user_id)
}
inline void IMP2PCmdMsg::set_from_user_id(const char* value,
    size_t size) {
  
  from_user_id_.SetLite(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), ::std::string(
      reinterpret_cast<const char*>(value), size), GetArena());
  // @@protoc_insertion_point(field_set_pointer:IM.SwitchService.IMP2PCmdMsg.from_user_id)
}
inline std::string* IMP2PCmdMsg::_internal_mutable_from_user_id() {
  
  return from_user_id_.Mutable(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), GetArena());
}
inline std::string* IMP2PCmdMsg::release_from_user_id() {
  // @@protoc_insertion_point(field_release:IM.SwitchService.IMP2PCmdMsg.from_user_id)
  return from_user_id_.Release(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), GetArena());
}
inline void IMP2PCmdMsg::set_allocated_from_user_id(std::string* from_user_id) {
  if (from_user_id != nullptr) {
    
  } else {
    
  }
  from_user_id_.SetAllocated(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), from_user_id,
      GetArena());
  // @@protoc_insertion_point(field_set_allocated:IM.SwitchService.IMP2PCmdMsg.from_user_id)
}

// string to_user_id = 2;
inline void IMP2PCmdMsg::clear_to_user_id() {
  to_user_id_.ClearToEmpty(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), GetArena());
}
inline const std::string& IMP2PCmdMsg::to_user_id() const {
  // @@protoc_insertion_point(field_get:IM.SwitchService.IMP2PCmdMsg.to_user_id)
  return _internal_to_user_id();
}
inline void IMP2PCmdMsg::set_to_user_id(const std::string& value) {
  _internal_set_to_user_id(value);
  // @@protoc_insertion_point(field_set:IM.SwitchService.IMP2PCmdMsg.to_user_id)
}
inline std::string* IMP2PCmdMsg::mutable_to_user_id() {
  // @@protoc_insertion_point(field_mutable:IM.SwitchService.IMP2PCmdMsg.to_user_id)
  return _internal_mutable_to_user_id();
}
inline const std::string& IMP2PCmdMsg::_internal_to_user_id() const {
  return to_user_id_.Get();
}
inline void IMP2PCmdMsg::_internal_set_to_user_id(const std::string& value) {
  
  to_user_id_.SetLite(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), value, GetArena());
}
inline void IMP2PCmdMsg::set_to_user_id(std::string&& value) {
  
  to_user_id_.SetLite(
    &::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), ::std::move(value), GetArena());
  // @@protoc_insertion_point(field_set_rvalue:IM.SwitchService.IMP2PCmdMsg.to_user_id)
}
inline void IMP2PCmdMsg::set_to_user_id(const char* value) {
  GOOGLE_DCHECK(value != nullptr);
  
  to_user_id_.SetLite(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), ::std::string(value),
              GetArena());
  // @@protoc_insertion_point(field_set_char:IM.SwitchService.IMP2PCmdMsg.to_user_id)
}
inline void IMP2PCmdMsg::set_to_user_id(const char* value,
    size_t size) {
  
  to_user_id_.SetLite(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), ::std::string(
      reinterpret_cast<const char*>(value), size), GetArena());
  // @@protoc_insertion_point(field_set_pointer:IM.SwitchService.IMP2PCmdMsg.to_user_id)
}
inline std::string* IMP2PCmdMsg::_internal_mutable_to_user_id() {
  
  return to_user_id_.Mutable(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), GetArena());
}
inline std::string* IMP2PCmdMsg::release_to_user_id() {
  // @@protoc_insertion_point(field_release:IM.SwitchService.IMP2PCmdMsg.to_user_id)
  return to_user_id_.Release(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), GetArena());
}
inline void IMP2PCmdMsg::set_allocated_to_user_id(std::string* to_user_id) {
  if (to_user_id != nullptr) {
    
  } else {
    
  }
  to_user_id_.SetAllocated(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), to_user_id,
      GetArena());
  // @@protoc_insertion_point(field_set_allocated:IM.SwitchService.IMP2PCmdMsg.to_user_id)
}

// string cmd_msg_data = 3;
inline void IMP2PCmdMsg::clear_cmd_msg_data() {
  cmd_msg_data_.ClearToEmpty(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), GetArena());
}
inline const std::string& IMP2PCmdMsg::cmd_msg_data() const {
  // @@protoc_insertion_point(field_get:IM.SwitchService.IMP2PCmdMsg.cmd_msg_data)
  return _internal_cmd_msg_data();
}
inline void IMP2PCmdMsg::set_cmd_msg_data(const std::string& value) {
  _internal_set_cmd_msg_data(value);
  // @@protoc_insertion_point(field_set:IM.SwitchService.IMP2PCmdMsg.cmd_msg_data)
}
inline std::string* IMP2PCmdMsg::mutable_cmd_msg_data() {
  // @@protoc_insertion_point(field_mutable:IM.SwitchService.IMP2PCmdMsg.cmd_msg_data)
  return _internal_mutable_cmd_msg_data();
}
inline const std::string& IMP2PCmdMsg::_internal_cmd_msg_data() const {
  return cmd_msg_data_.Get();
}
inline void IMP2PCmdMsg::_internal_set_cmd_msg_data(const std::string& value) {
  
  cmd_msg_data_.SetLite(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), value, GetArena());
}
inline void IMP2PCmdMsg::set_cmd_msg_data(std::string&& value) {
  
  cmd_msg_data_.SetLite(
    &::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), ::std::move(value), GetArena());
  // @@protoc_insertion_point(field_set_rvalue:IM.SwitchService.IMP2PCmdMsg.cmd_msg_data)
}
inline void IMP2PCmdMsg::set_cmd_msg_data(const char* value) {
  GOOGLE_DCHECK(value != nullptr);
  
  cmd_msg_data_.SetLite(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), ::std::string(value),
              GetArena());
  // @@protoc_insertion_point(field_set_char:IM.SwitchService.IMP2PCmdMsg.cmd_msg_data)
}
inline void IMP2PCmdMsg::set_cmd_msg_data(const char* value,
    size_t size) {
  
  cmd_msg_data_.SetLite(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), ::std::string(
      reinterpret_cast<const char*>(value), size), GetArena());
  // @@protoc_insertion_point(field_set_pointer:IM.SwitchService.IMP2PCmdMsg.cmd_msg_data)
}
inline std::string* IMP2PCmdMsg::_internal_mutable_cmd_msg_data() {
  
  return cmd_msg_data_.Mutable(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), GetArena());
}
inline std::string* IMP2PCmdMsg::release_cmd_msg_data() {
  // @@protoc_insertion_point(field_release:IM.SwitchService.IMP2PCmdMsg.cmd_msg_data)
  return cmd_msg_data_.Release(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), GetArena());
}
inline void IMP2PCmdMsg::set_allocated_cmd_msg_data(std::string* cmd_msg_data) {
  if (cmd_msg_data != nullptr) {
    
  } else {
    
  }
  cmd_msg_data_.SetAllocated(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), cmd_msg_data,
      GetArena());
  // @@protoc_insertion_point(field_set_allocated:IM.SwitchService.IMP2PCmdMsg.cmd_msg_data)
}

#ifdef __GNUC__
  #pragma GCC diagnostic pop
#endif  // __GNUC__

// @@protoc_insertion_point(namespace_scope)

}  // namespace SwitchService
}  // namespace IM

// @@protoc_insertion_point(global_scope)

#include <google/protobuf/port_undef.inc>
#endif  // GOOGLE_PROTOBUF_INCLUDED_GOOGLE_PROTOBUF_INCLUDED_IM_2eSwitchService_2eproto
