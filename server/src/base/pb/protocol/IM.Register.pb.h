// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: IM.Register.proto

#ifndef GOOGLE_PROTOBUF_INCLUDED_IM_2eRegister_2eproto
#define GOOGLE_PROTOBUF_INCLUDED_IM_2eRegister_2eproto

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
#include "IM.BaseDefine.pb.h"
// @@protoc_insertion_point(includes)
#include <google/protobuf/port_def.inc>
#define PROTOBUF_INTERNAL_EXPORT_IM_2eRegister_2eproto
PROTOBUF_NAMESPACE_OPEN
namespace internal {
class AnyMetadata;
}  // namespace internal
PROTOBUF_NAMESPACE_CLOSE

// Internal implementation detail -- do not use these members.
struct TableStruct_IM_2eRegister_2eproto {
  static const ::PROTOBUF_NAMESPACE_ID::internal::ParseTableField entries[]
    PROTOBUF_SECTION_VARIABLE(protodesc_cold);
  static const ::PROTOBUF_NAMESPACE_ID::internal::AuxiliaryParseTableField aux[]
    PROTOBUF_SECTION_VARIABLE(protodesc_cold);
  static const ::PROTOBUF_NAMESPACE_ID::internal::ParseTable schema[2]
    PROTOBUF_SECTION_VARIABLE(protodesc_cold);
  static const ::PROTOBUF_NAMESPACE_ID::internal::FieldMetadata field_metadata[];
  static const ::PROTOBUF_NAMESPACE_ID::internal::SerializationTable serialization_table[];
  static const ::PROTOBUF_NAMESPACE_ID::uint32 offsets[];
};
namespace IM {
namespace Register {
class IMRegisterReq;
class IMRegisterReqDefaultTypeInternal;
extern IMRegisterReqDefaultTypeInternal _IMRegisterReq_default_instance_;
class IMRegisterRsp;
class IMRegisterRspDefaultTypeInternal;
extern IMRegisterRspDefaultTypeInternal _IMRegisterRsp_default_instance_;
}  // namespace Register
}  // namespace IM
PROTOBUF_NAMESPACE_OPEN
template<> ::IM::Register::IMRegisterReq* Arena::CreateMaybeMessage<::IM::Register::IMRegisterReq>(Arena*);
template<> ::IM::Register::IMRegisterRsp* Arena::CreateMaybeMessage<::IM::Register::IMRegisterRsp>(Arena*);
PROTOBUF_NAMESPACE_CLOSE
namespace IM {
namespace Register {

// ===================================================================

class IMRegisterReq PROTOBUF_FINAL :
    public ::PROTOBUF_NAMESPACE_ID::MessageLite /* @@protoc_insertion_point(class_definition:IM.Register.IMRegisterReq) */ {
 public:
  inline IMRegisterReq() : IMRegisterReq(nullptr) {};
  virtual ~IMRegisterReq();

  IMRegisterReq(const IMRegisterReq& from);
  IMRegisterReq(IMRegisterReq&& from) noexcept
    : IMRegisterReq() {
    *this = ::std::move(from);
  }

  inline IMRegisterReq& operator=(const IMRegisterReq& from) {
    CopyFrom(from);
    return *this;
  }
  inline IMRegisterReq& operator=(IMRegisterReq&& from) noexcept {
    if (GetArena() == from.GetArena()) {
      if (this != &from) InternalSwap(&from);
    } else {
      CopyFrom(from);
    }
    return *this;
  }

  static const IMRegisterReq& default_instance();

  static void InitAsDefaultInstance();  // FOR INTERNAL USE ONLY
  static inline const IMRegisterReq* internal_default_instance() {
    return reinterpret_cast<const IMRegisterReq*>(
               &_IMRegisterReq_default_instance_);
  }
  static constexpr int kIndexInFileMessages =
    0;

  friend void swap(IMRegisterReq& a, IMRegisterReq& b) {
    a.Swap(&b);
  }
  inline void Swap(IMRegisterReq* other) {
    if (other == this) return;
    if (GetArena() == other->GetArena()) {
      InternalSwap(other);
    } else {
      ::PROTOBUF_NAMESPACE_ID::internal::GenericSwap(this, other);
    }
  }
  void UnsafeArenaSwap(IMRegisterReq* other) {
    if (other == this) return;
    GOOGLE_DCHECK(GetArena() == other->GetArena());
    InternalSwap(other);
  }

  // implements Message ----------------------------------------------

  inline IMRegisterReq* New() const final {
    return CreateMaybeMessage<IMRegisterReq>(nullptr);
  }

  IMRegisterReq* New(::PROTOBUF_NAMESPACE_ID::Arena* arena) const final {
    return CreateMaybeMessage<IMRegisterReq>(arena);
  }
  void CheckTypeAndMergeFrom(const ::PROTOBUF_NAMESPACE_ID::MessageLite& from)
    final;
  void CopyFrom(const IMRegisterReq& from);
  void MergeFrom(const IMRegisterReq& from);
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
  void InternalSwap(IMRegisterReq* other);
  friend class ::PROTOBUF_NAMESPACE_ID::internal::AnyMetadata;
  static ::PROTOBUF_NAMESPACE_ID::StringPiece FullMessageName() {
    return "IM.Register.IMRegisterReq";
  }
  protected:
  explicit IMRegisterReq(::PROTOBUF_NAMESPACE_ID::Arena* arena);
  private:
  static void ArenaDtor(void* object);
  inline void RegisterArenaDtor(::PROTOBUF_NAMESPACE_ID::Arena* arena);
  public:

  std::string GetTypeName() const final;

  // nested types ----------------------------------------------------

  // accessors -------------------------------------------------------

  enum : int {
    kUserNameFieldNumber = 1,
    kPasswordFieldNumber = 2,
    kAttachDataFieldNumber = 20,
  };
  // string user_name = 1;
  void clear_user_name();
  const std::string& user_name() const;
  void set_user_name(const std::string& value);
  void set_user_name(std::string&& value);
  void set_user_name(const char* value);
  void set_user_name(const char* value, size_t size);
  std::string* mutable_user_name();
  std::string* release_user_name();
  void set_allocated_user_name(std::string* user_name);
  private:
  const std::string& _internal_user_name() const;
  void _internal_set_user_name(const std::string& value);
  std::string* _internal_mutable_user_name();
  public:

  // string password = 2;
  void clear_password();
  const std::string& password() const;
  void set_password(const std::string& value);
  void set_password(std::string&& value);
  void set_password(const char* value);
  void set_password(const char* value, size_t size);
  std::string* mutable_password();
  std::string* release_password();
  void set_allocated_password(std::string* password);
  private:
  const std::string& _internal_password() const;
  void _internal_set_password(const std::string& value);
  std::string* _internal_mutable_password();
  public:

  // bytes attach_data = 20;
  void clear_attach_data();
  const std::string& attach_data() const;
  void set_attach_data(const std::string& value);
  void set_attach_data(std::string&& value);
  void set_attach_data(const char* value);
  void set_attach_data(const void* value, size_t size);
  std::string* mutable_attach_data();
  std::string* release_attach_data();
  void set_allocated_attach_data(std::string* attach_data);
  private:
  const std::string& _internal_attach_data() const;
  void _internal_set_attach_data(const std::string& value);
  std::string* _internal_mutable_attach_data();
  public:

  // @@protoc_insertion_point(class_scope:IM.Register.IMRegisterReq)
 private:
  class _Internal;

  template <typename T> friend class ::PROTOBUF_NAMESPACE_ID::Arena::InternalHelper;
  typedef void InternalArenaConstructable_;
  typedef void DestructorSkippable_;
  ::PROTOBUF_NAMESPACE_ID::internal::ArenaStringPtr user_name_;
  ::PROTOBUF_NAMESPACE_ID::internal::ArenaStringPtr password_;
  ::PROTOBUF_NAMESPACE_ID::internal::ArenaStringPtr attach_data_;
  mutable ::PROTOBUF_NAMESPACE_ID::internal::CachedSize _cached_size_;
  friend struct ::TableStruct_IM_2eRegister_2eproto;
};
// -------------------------------------------------------------------

class IMRegisterRsp PROTOBUF_FINAL :
    public ::PROTOBUF_NAMESPACE_ID::MessageLite /* @@protoc_insertion_point(class_definition:IM.Register.IMRegisterRsp) */ {
 public:
  inline IMRegisterRsp() : IMRegisterRsp(nullptr) {};
  virtual ~IMRegisterRsp();

  IMRegisterRsp(const IMRegisterRsp& from);
  IMRegisterRsp(IMRegisterRsp&& from) noexcept
    : IMRegisterRsp() {
    *this = ::std::move(from);
  }

  inline IMRegisterRsp& operator=(const IMRegisterRsp& from) {
    CopyFrom(from);
    return *this;
  }
  inline IMRegisterRsp& operator=(IMRegisterRsp&& from) noexcept {
    if (GetArena() == from.GetArena()) {
      if (this != &from) InternalSwap(&from);
    } else {
      CopyFrom(from);
    }
    return *this;
  }

  static const IMRegisterRsp& default_instance();

  static void InitAsDefaultInstance();  // FOR INTERNAL USE ONLY
  static inline const IMRegisterRsp* internal_default_instance() {
    return reinterpret_cast<const IMRegisterRsp*>(
               &_IMRegisterRsp_default_instance_);
  }
  static constexpr int kIndexInFileMessages =
    1;

  friend void swap(IMRegisterRsp& a, IMRegisterRsp& b) {
    a.Swap(&b);
  }
  inline void Swap(IMRegisterRsp* other) {
    if (other == this) return;
    if (GetArena() == other->GetArena()) {
      InternalSwap(other);
    } else {
      ::PROTOBUF_NAMESPACE_ID::internal::GenericSwap(this, other);
    }
  }
  void UnsafeArenaSwap(IMRegisterRsp* other) {
    if (other == this) return;
    GOOGLE_DCHECK(GetArena() == other->GetArena());
    InternalSwap(other);
  }

  // implements Message ----------------------------------------------

  inline IMRegisterRsp* New() const final {
    return CreateMaybeMessage<IMRegisterRsp>(nullptr);
  }

  IMRegisterRsp* New(::PROTOBUF_NAMESPACE_ID::Arena* arena) const final {
    return CreateMaybeMessage<IMRegisterRsp>(arena);
  }
  void CheckTypeAndMergeFrom(const ::PROTOBUF_NAMESPACE_ID::MessageLite& from)
    final;
  void CopyFrom(const IMRegisterRsp& from);
  void MergeFrom(const IMRegisterRsp& from);
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
  void InternalSwap(IMRegisterRsp* other);
  friend class ::PROTOBUF_NAMESPACE_ID::internal::AnyMetadata;
  static ::PROTOBUF_NAMESPACE_ID::StringPiece FullMessageName() {
    return "IM.Register.IMRegisterRsp";
  }
  protected:
  explicit IMRegisterRsp(::PROTOBUF_NAMESPACE_ID::Arena* arena);
  private:
  static void ArenaDtor(void* object);
  inline void RegisterArenaDtor(::PROTOBUF_NAMESPACE_ID::Arena* arena);
  public:

  std::string GetTypeName() const final;

  // nested types ----------------------------------------------------

  // accessors -------------------------------------------------------

  enum : int {
    kResultStringFieldNumber = 2,
    kAttachDataFieldNumber = 20,
    kResultCodeFieldNumber = 1,
  };
  // string result_string = 2;
  void clear_result_string();
  const std::string& result_string() const;
  void set_result_string(const std::string& value);
  void set_result_string(std::string&& value);
  void set_result_string(const char* value);
  void set_result_string(const char* value, size_t size);
  std::string* mutable_result_string();
  std::string* release_result_string();
  void set_allocated_result_string(std::string* result_string);
  private:
  const std::string& _internal_result_string() const;
  void _internal_set_result_string(const std::string& value);
  std::string* _internal_mutable_result_string();
  public:

  // bytes attach_data = 20;
  void clear_attach_data();
  const std::string& attach_data() const;
  void set_attach_data(const std::string& value);
  void set_attach_data(std::string&& value);
  void set_attach_data(const char* value);
  void set_attach_data(const void* value, size_t size);
  std::string* mutable_attach_data();
  std::string* release_attach_data();
  void set_allocated_attach_data(std::string* attach_data);
  private:
  const std::string& _internal_attach_data() const;
  void _internal_set_attach_data(const std::string& value);
  std::string* _internal_mutable_attach_data();
  public:

  // .IM.BaseDefine.ResultType result_code = 1;
  void clear_result_code();
  ::IM::BaseDefine::ResultType result_code() const;
  void set_result_code(::IM::BaseDefine::ResultType value);
  private:
  ::IM::BaseDefine::ResultType _internal_result_code() const;
  void _internal_set_result_code(::IM::BaseDefine::ResultType value);
  public:

  // @@protoc_insertion_point(class_scope:IM.Register.IMRegisterRsp)
 private:
  class _Internal;

  template <typename T> friend class ::PROTOBUF_NAMESPACE_ID::Arena::InternalHelper;
  typedef void InternalArenaConstructable_;
  typedef void DestructorSkippable_;
  ::PROTOBUF_NAMESPACE_ID::internal::ArenaStringPtr result_string_;
  ::PROTOBUF_NAMESPACE_ID::internal::ArenaStringPtr attach_data_;
  int result_code_;
  mutable ::PROTOBUF_NAMESPACE_ID::internal::CachedSize _cached_size_;
  friend struct ::TableStruct_IM_2eRegister_2eproto;
};
// ===================================================================


// ===================================================================

#ifdef __GNUC__
  #pragma GCC diagnostic push
  #pragma GCC diagnostic ignored "-Wstrict-aliasing"
#endif  // __GNUC__
// IMRegisterReq

// string user_name = 1;
inline void IMRegisterReq::clear_user_name() {
  user_name_.ClearToEmpty(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), GetArena());
}
inline const std::string& IMRegisterReq::user_name() const {
  // @@protoc_insertion_point(field_get:IM.Register.IMRegisterReq.user_name)
  return _internal_user_name();
}
inline void IMRegisterReq::set_user_name(const std::string& value) {
  _internal_set_user_name(value);
  // @@protoc_insertion_point(field_set:IM.Register.IMRegisterReq.user_name)
}
inline std::string* IMRegisterReq::mutable_user_name() {
  // @@protoc_insertion_point(field_mutable:IM.Register.IMRegisterReq.user_name)
  return _internal_mutable_user_name();
}
inline const std::string& IMRegisterReq::_internal_user_name() const {
  return user_name_.Get();
}
inline void IMRegisterReq::_internal_set_user_name(const std::string& value) {
  
  user_name_.SetLite(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), value, GetArena());
}
inline void IMRegisterReq::set_user_name(std::string&& value) {
  
  user_name_.SetLite(
    &::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), ::std::move(value), GetArena());
  // @@protoc_insertion_point(field_set_rvalue:IM.Register.IMRegisterReq.user_name)
}
inline void IMRegisterReq::set_user_name(const char* value) {
  GOOGLE_DCHECK(value != nullptr);
  
  user_name_.SetLite(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), ::std::string(value),
              GetArena());
  // @@protoc_insertion_point(field_set_char:IM.Register.IMRegisterReq.user_name)
}
inline void IMRegisterReq::set_user_name(const char* value,
    size_t size) {
  
  user_name_.SetLite(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), ::std::string(
      reinterpret_cast<const char*>(value), size), GetArena());
  // @@protoc_insertion_point(field_set_pointer:IM.Register.IMRegisterReq.user_name)
}
inline std::string* IMRegisterReq::_internal_mutable_user_name() {
  
  return user_name_.Mutable(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), GetArena());
}
inline std::string* IMRegisterReq::release_user_name() {
  // @@protoc_insertion_point(field_release:IM.Register.IMRegisterReq.user_name)
  return user_name_.Release(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), GetArena());
}
inline void IMRegisterReq::set_allocated_user_name(std::string* user_name) {
  if (user_name != nullptr) {
    
  } else {
    
  }
  user_name_.SetAllocated(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), user_name,
      GetArena());
  // @@protoc_insertion_point(field_set_allocated:IM.Register.IMRegisterReq.user_name)
}

// string password = 2;
inline void IMRegisterReq::clear_password() {
  password_.ClearToEmpty(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), GetArena());
}
inline const std::string& IMRegisterReq::password() const {
  // @@protoc_insertion_point(field_get:IM.Register.IMRegisterReq.password)
  return _internal_password();
}
inline void IMRegisterReq::set_password(const std::string& value) {
  _internal_set_password(value);
  // @@protoc_insertion_point(field_set:IM.Register.IMRegisterReq.password)
}
inline std::string* IMRegisterReq::mutable_password() {
  // @@protoc_insertion_point(field_mutable:IM.Register.IMRegisterReq.password)
  return _internal_mutable_password();
}
inline const std::string& IMRegisterReq::_internal_password() const {
  return password_.Get();
}
inline void IMRegisterReq::_internal_set_password(const std::string& value) {
  
  password_.SetLite(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), value, GetArena());
}
inline void IMRegisterReq::set_password(std::string&& value) {
  
  password_.SetLite(
    &::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), ::std::move(value), GetArena());
  // @@protoc_insertion_point(field_set_rvalue:IM.Register.IMRegisterReq.password)
}
inline void IMRegisterReq::set_password(const char* value) {
  GOOGLE_DCHECK(value != nullptr);
  
  password_.SetLite(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), ::std::string(value),
              GetArena());
  // @@protoc_insertion_point(field_set_char:IM.Register.IMRegisterReq.password)
}
inline void IMRegisterReq::set_password(const char* value,
    size_t size) {
  
  password_.SetLite(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), ::std::string(
      reinterpret_cast<const char*>(value), size), GetArena());
  // @@protoc_insertion_point(field_set_pointer:IM.Register.IMRegisterReq.password)
}
inline std::string* IMRegisterReq::_internal_mutable_password() {
  
  return password_.Mutable(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), GetArena());
}
inline std::string* IMRegisterReq::release_password() {
  // @@protoc_insertion_point(field_release:IM.Register.IMRegisterReq.password)
  return password_.Release(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), GetArena());
}
inline void IMRegisterReq::set_allocated_password(std::string* password) {
  if (password != nullptr) {
    
  } else {
    
  }
  password_.SetAllocated(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), password,
      GetArena());
  // @@protoc_insertion_point(field_set_allocated:IM.Register.IMRegisterReq.password)
}

// bytes attach_data = 20;
inline void IMRegisterReq::clear_attach_data() {
  attach_data_.ClearToEmpty(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), GetArena());
}
inline const std::string& IMRegisterReq::attach_data() const {
  // @@protoc_insertion_point(field_get:IM.Register.IMRegisterReq.attach_data)
  return _internal_attach_data();
}
inline void IMRegisterReq::set_attach_data(const std::string& value) {
  _internal_set_attach_data(value);
  // @@protoc_insertion_point(field_set:IM.Register.IMRegisterReq.attach_data)
}
inline std::string* IMRegisterReq::mutable_attach_data() {
  // @@protoc_insertion_point(field_mutable:IM.Register.IMRegisterReq.attach_data)
  return _internal_mutable_attach_data();
}
inline const std::string& IMRegisterReq::_internal_attach_data() const {
  return attach_data_.Get();
}
inline void IMRegisterReq::_internal_set_attach_data(const std::string& value) {
  
  attach_data_.SetLite(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), value, GetArena());
}
inline void IMRegisterReq::set_attach_data(std::string&& value) {
  
  attach_data_.SetLite(
    &::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), ::std::move(value), GetArena());
  // @@protoc_insertion_point(field_set_rvalue:IM.Register.IMRegisterReq.attach_data)
}
inline void IMRegisterReq::set_attach_data(const char* value) {
  GOOGLE_DCHECK(value != nullptr);
  
  attach_data_.SetLite(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), ::std::string(value),
              GetArena());
  // @@protoc_insertion_point(field_set_char:IM.Register.IMRegisterReq.attach_data)
}
inline void IMRegisterReq::set_attach_data(const void* value,
    size_t size) {
  
  attach_data_.SetLite(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), ::std::string(
      reinterpret_cast<const char*>(value), size), GetArena());
  // @@protoc_insertion_point(field_set_pointer:IM.Register.IMRegisterReq.attach_data)
}
inline std::string* IMRegisterReq::_internal_mutable_attach_data() {
  
  return attach_data_.Mutable(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), GetArena());
}
inline std::string* IMRegisterReq::release_attach_data() {
  // @@protoc_insertion_point(field_release:IM.Register.IMRegisterReq.attach_data)
  return attach_data_.Release(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), GetArena());
}
inline void IMRegisterReq::set_allocated_attach_data(std::string* attach_data) {
  if (attach_data != nullptr) {
    
  } else {
    
  }
  attach_data_.SetAllocated(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), attach_data,
      GetArena());
  // @@protoc_insertion_point(field_set_allocated:IM.Register.IMRegisterReq.attach_data)
}

// -------------------------------------------------------------------

// IMRegisterRsp

// .IM.BaseDefine.ResultType result_code = 1;
inline void IMRegisterRsp::clear_result_code() {
  result_code_ = 0;
}
inline ::IM::BaseDefine::ResultType IMRegisterRsp::_internal_result_code() const {
  return static_cast< ::IM::BaseDefine::ResultType >(result_code_);
}
inline ::IM::BaseDefine::ResultType IMRegisterRsp::result_code() const {
  // @@protoc_insertion_point(field_get:IM.Register.IMRegisterRsp.result_code)
  return _internal_result_code();
}
inline void IMRegisterRsp::_internal_set_result_code(::IM::BaseDefine::ResultType value) {
  
  result_code_ = value;
}
inline void IMRegisterRsp::set_result_code(::IM::BaseDefine::ResultType value) {
  _internal_set_result_code(value);
  // @@protoc_insertion_point(field_set:IM.Register.IMRegisterRsp.result_code)
}

// string result_string = 2;
inline void IMRegisterRsp::clear_result_string() {
  result_string_.ClearToEmpty(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), GetArena());
}
inline const std::string& IMRegisterRsp::result_string() const {
  // @@protoc_insertion_point(field_get:IM.Register.IMRegisterRsp.result_string)
  return _internal_result_string();
}
inline void IMRegisterRsp::set_result_string(const std::string& value) {
  _internal_set_result_string(value);
  // @@protoc_insertion_point(field_set:IM.Register.IMRegisterRsp.result_string)
}
inline std::string* IMRegisterRsp::mutable_result_string() {
  // @@protoc_insertion_point(field_mutable:IM.Register.IMRegisterRsp.result_string)
  return _internal_mutable_result_string();
}
inline const std::string& IMRegisterRsp::_internal_result_string() const {
  return result_string_.Get();
}
inline void IMRegisterRsp::_internal_set_result_string(const std::string& value) {
  
  result_string_.SetLite(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), value, GetArena());
}
inline void IMRegisterRsp::set_result_string(std::string&& value) {
  
  result_string_.SetLite(
    &::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), ::std::move(value), GetArena());
  // @@protoc_insertion_point(field_set_rvalue:IM.Register.IMRegisterRsp.result_string)
}
inline void IMRegisterRsp::set_result_string(const char* value) {
  GOOGLE_DCHECK(value != nullptr);
  
  result_string_.SetLite(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), ::std::string(value),
              GetArena());
  // @@protoc_insertion_point(field_set_char:IM.Register.IMRegisterRsp.result_string)
}
inline void IMRegisterRsp::set_result_string(const char* value,
    size_t size) {
  
  result_string_.SetLite(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), ::std::string(
      reinterpret_cast<const char*>(value), size), GetArena());
  // @@protoc_insertion_point(field_set_pointer:IM.Register.IMRegisterRsp.result_string)
}
inline std::string* IMRegisterRsp::_internal_mutable_result_string() {
  
  return result_string_.Mutable(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), GetArena());
}
inline std::string* IMRegisterRsp::release_result_string() {
  // @@protoc_insertion_point(field_release:IM.Register.IMRegisterRsp.result_string)
  return result_string_.Release(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), GetArena());
}
inline void IMRegisterRsp::set_allocated_result_string(std::string* result_string) {
  if (result_string != nullptr) {
    
  } else {
    
  }
  result_string_.SetAllocated(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), result_string,
      GetArena());
  // @@protoc_insertion_point(field_set_allocated:IM.Register.IMRegisterRsp.result_string)
}

// bytes attach_data = 20;
inline void IMRegisterRsp::clear_attach_data() {
  attach_data_.ClearToEmpty(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), GetArena());
}
inline const std::string& IMRegisterRsp::attach_data() const {
  // @@protoc_insertion_point(field_get:IM.Register.IMRegisterRsp.attach_data)
  return _internal_attach_data();
}
inline void IMRegisterRsp::set_attach_data(const std::string& value) {
  _internal_set_attach_data(value);
  // @@protoc_insertion_point(field_set:IM.Register.IMRegisterRsp.attach_data)
}
inline std::string* IMRegisterRsp::mutable_attach_data() {
  // @@protoc_insertion_point(field_mutable:IM.Register.IMRegisterRsp.attach_data)
  return _internal_mutable_attach_data();
}
inline const std::string& IMRegisterRsp::_internal_attach_data() const {
  return attach_data_.Get();
}
inline void IMRegisterRsp::_internal_set_attach_data(const std::string& value) {
  
  attach_data_.SetLite(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), value, GetArena());
}
inline void IMRegisterRsp::set_attach_data(std::string&& value) {
  
  attach_data_.SetLite(
    &::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), ::std::move(value), GetArena());
  // @@protoc_insertion_point(field_set_rvalue:IM.Register.IMRegisterRsp.attach_data)
}
inline void IMRegisterRsp::set_attach_data(const char* value) {
  GOOGLE_DCHECK(value != nullptr);
  
  attach_data_.SetLite(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), ::std::string(value),
              GetArena());
  // @@protoc_insertion_point(field_set_char:IM.Register.IMRegisterRsp.attach_data)
}
inline void IMRegisterRsp::set_attach_data(const void* value,
    size_t size) {
  
  attach_data_.SetLite(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), ::std::string(
      reinterpret_cast<const char*>(value), size), GetArena());
  // @@protoc_insertion_point(field_set_pointer:IM.Register.IMRegisterRsp.attach_data)
}
inline std::string* IMRegisterRsp::_internal_mutable_attach_data() {
  
  return attach_data_.Mutable(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), GetArena());
}
inline std::string* IMRegisterRsp::release_attach_data() {
  // @@protoc_insertion_point(field_release:IM.Register.IMRegisterRsp.attach_data)
  return attach_data_.Release(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), GetArena());
}
inline void IMRegisterRsp::set_allocated_attach_data(std::string* attach_data) {
  if (attach_data != nullptr) {
    
  } else {
    
  }
  attach_data_.SetAllocated(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), attach_data,
      GetArena());
  // @@protoc_insertion_point(field_set_allocated:IM.Register.IMRegisterRsp.attach_data)
}

#ifdef __GNUC__
  #pragma GCC diagnostic pop
#endif  // __GNUC__
// -------------------------------------------------------------------


// @@protoc_insertion_point(namespace_scope)

}  // namespace Register
}  // namespace IM

// @@protoc_insertion_point(global_scope)

#include <google/protobuf/port_undef.inc>
#endif  // GOOGLE_PROTOBUF_INCLUDED_GOOGLE_PROTOBUF_INCLUDED_IM_2eRegister_2eproto
