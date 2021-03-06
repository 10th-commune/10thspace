// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: IM.SwitchService.proto

#include "IM.SwitchService.pb.h"

#include <algorithm>

#include <google/protobuf/io/coded_stream.h>
#include <google/protobuf/extension_set.h>
#include <google/protobuf/wire_format_lite.h>
#include <google/protobuf/io/zero_copy_stream_impl_lite.h>
// @@protoc_insertion_point(includes)
#include <google/protobuf/port_def.inc>
namespace IM {
namespace SwitchService {
class IMP2PCmdMsgDefaultTypeInternal {
 public:
  ::PROTOBUF_NAMESPACE_ID::internal::ExplicitlyConstructed<IMP2PCmdMsg> _instance;
} _IMP2PCmdMsg_default_instance_;
}  // namespace SwitchService
}  // namespace IM
static void InitDefaultsscc_info_IMP2PCmdMsg_IM_2eSwitchService_2eproto() {
  GOOGLE_PROTOBUF_VERIFY_VERSION;

  {
    void* ptr = &::IM::SwitchService::_IMP2PCmdMsg_default_instance_;
    new (ptr) ::IM::SwitchService::IMP2PCmdMsg();
    ::PROTOBUF_NAMESPACE_ID::internal::OnShutdownDestroyMessage(ptr);
  }
  ::IM::SwitchService::IMP2PCmdMsg::InitAsDefaultInstance();
}

::PROTOBUF_NAMESPACE_ID::internal::SCCInfo<0> scc_info_IMP2PCmdMsg_IM_2eSwitchService_2eproto =
    {{ATOMIC_VAR_INIT(::PROTOBUF_NAMESPACE_ID::internal::SCCInfoBase::kUninitialized), 0, 0, InitDefaultsscc_info_IMP2PCmdMsg_IM_2eSwitchService_2eproto}, {}};

namespace IM {
namespace SwitchService {

// ===================================================================

void IMP2PCmdMsg::InitAsDefaultInstance() {
}
class IMP2PCmdMsg::_Internal {
 public:
};

IMP2PCmdMsg::IMP2PCmdMsg(::PROTOBUF_NAMESPACE_ID::Arena* arena)
  : ::PROTOBUF_NAMESPACE_ID::MessageLite(arena) {
  SharedCtor();
  RegisterArenaDtor(arena);
  // @@protoc_insertion_point(arena_constructor:IM.SwitchService.IMP2PCmdMsg)
}
IMP2PCmdMsg::IMP2PCmdMsg(const IMP2PCmdMsg& from)
  : ::PROTOBUF_NAMESPACE_ID::MessageLite() {
  _internal_metadata_.MergeFrom<std::string>(from._internal_metadata_);
  from_user_id_.UnsafeSetDefault(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited());
  if (!from._internal_from_user_id().empty()) {
    from_user_id_.SetLite(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), from._internal_from_user_id(),
      GetArena());
  }
  to_user_id_.UnsafeSetDefault(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited());
  if (!from._internal_to_user_id().empty()) {
    to_user_id_.SetLite(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), from._internal_to_user_id(),
      GetArena());
  }
  cmd_msg_data_.UnsafeSetDefault(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited());
  if (!from._internal_cmd_msg_data().empty()) {
    cmd_msg_data_.SetLite(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), from._internal_cmd_msg_data(),
      GetArena());
  }
  // @@protoc_insertion_point(copy_constructor:IM.SwitchService.IMP2PCmdMsg)
}

void IMP2PCmdMsg::SharedCtor() {
  ::PROTOBUF_NAMESPACE_ID::internal::InitSCC(&scc_info_IMP2PCmdMsg_IM_2eSwitchService_2eproto.base);
  from_user_id_.UnsafeSetDefault(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited());
  to_user_id_.UnsafeSetDefault(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited());
  cmd_msg_data_.UnsafeSetDefault(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited());
}

IMP2PCmdMsg::~IMP2PCmdMsg() {
  // @@protoc_insertion_point(destructor:IM.SwitchService.IMP2PCmdMsg)
  SharedDtor();
  _internal_metadata_.Delete<std::string>();
}

void IMP2PCmdMsg::SharedDtor() {
  GOOGLE_DCHECK(GetArena() == nullptr);
  from_user_id_.DestroyNoArena(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited());
  to_user_id_.DestroyNoArena(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited());
  cmd_msg_data_.DestroyNoArena(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited());
}

void IMP2PCmdMsg::ArenaDtor(void* object) {
  IMP2PCmdMsg* _this = reinterpret_cast< IMP2PCmdMsg* >(object);
  (void)_this;
}
void IMP2PCmdMsg::RegisterArenaDtor(::PROTOBUF_NAMESPACE_ID::Arena*) {
}
void IMP2PCmdMsg::SetCachedSize(int size) const {
  _cached_size_.Set(size);
}
const IMP2PCmdMsg& IMP2PCmdMsg::default_instance() {
  ::PROTOBUF_NAMESPACE_ID::internal::InitSCC(&::scc_info_IMP2PCmdMsg_IM_2eSwitchService_2eproto.base);
  return *internal_default_instance();
}


void IMP2PCmdMsg::Clear() {
// @@protoc_insertion_point(message_clear_start:IM.SwitchService.IMP2PCmdMsg)
  ::PROTOBUF_NAMESPACE_ID::uint32 cached_has_bits = 0;
  // Prevent compiler warnings about cached_has_bits being unused
  (void) cached_has_bits;

  from_user_id_.ClearToEmpty(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), GetArena());
  to_user_id_.ClearToEmpty(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), GetArena());
  cmd_msg_data_.ClearToEmpty(&::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), GetArena());
  _internal_metadata_.Clear<std::string>();
}

const char* IMP2PCmdMsg::_InternalParse(const char* ptr, ::PROTOBUF_NAMESPACE_ID::internal::ParseContext* ctx) {
#define CHK_(x) if (PROTOBUF_PREDICT_FALSE(!(x))) goto failure
  ::PROTOBUF_NAMESPACE_ID::Arena* arena = GetArena(); (void)arena;
  while (!ctx->Done(&ptr)) {
    ::PROTOBUF_NAMESPACE_ID::uint32 tag;
    ptr = ::PROTOBUF_NAMESPACE_ID::internal::ReadTag(ptr, &tag);
    CHK_(ptr);
    switch (tag >> 3) {
      // string from_user_id = 1;
      case 1:
        if (PROTOBUF_PREDICT_TRUE(static_cast<::PROTOBUF_NAMESPACE_ID::uint8>(tag) == 10)) {
          auto str = _internal_mutable_from_user_id();
          ptr = ::PROTOBUF_NAMESPACE_ID::internal::InlineGreedyStringParser(str, ptr, ctx);
          CHK_(::PROTOBUF_NAMESPACE_ID::internal::VerifyUTF8(str, nullptr));
          CHK_(ptr);
        } else goto handle_unusual;
        continue;
      // string to_user_id = 2;
      case 2:
        if (PROTOBUF_PREDICT_TRUE(static_cast<::PROTOBUF_NAMESPACE_ID::uint8>(tag) == 18)) {
          auto str = _internal_mutable_to_user_id();
          ptr = ::PROTOBUF_NAMESPACE_ID::internal::InlineGreedyStringParser(str, ptr, ctx);
          CHK_(::PROTOBUF_NAMESPACE_ID::internal::VerifyUTF8(str, nullptr));
          CHK_(ptr);
        } else goto handle_unusual;
        continue;
      // string cmd_msg_data = 3;
      case 3:
        if (PROTOBUF_PREDICT_TRUE(static_cast<::PROTOBUF_NAMESPACE_ID::uint8>(tag) == 26)) {
          auto str = _internal_mutable_cmd_msg_data();
          ptr = ::PROTOBUF_NAMESPACE_ID::internal::InlineGreedyStringParser(str, ptr, ctx);
          CHK_(::PROTOBUF_NAMESPACE_ID::internal::VerifyUTF8(str, nullptr));
          CHK_(ptr);
        } else goto handle_unusual;
        continue;
      default: {
      handle_unusual:
        if ((tag & 7) == 4 || tag == 0) {
          ctx->SetLastTag(tag);
          goto success;
        }
        ptr = UnknownFieldParse(tag,
            _internal_metadata_.mutable_unknown_fields<std::string>(),
            ptr, ctx);
        CHK_(ptr != nullptr);
        continue;
      }
    }  // switch
  }  // while
success:
  return ptr;
failure:
  ptr = nullptr;
  goto success;
#undef CHK_
}

::PROTOBUF_NAMESPACE_ID::uint8* IMP2PCmdMsg::_InternalSerialize(
    ::PROTOBUF_NAMESPACE_ID::uint8* target, ::PROTOBUF_NAMESPACE_ID::io::EpsCopyOutputStream* stream) const {
  // @@protoc_insertion_point(serialize_to_array_start:IM.SwitchService.IMP2PCmdMsg)
  ::PROTOBUF_NAMESPACE_ID::uint32 cached_has_bits = 0;
  (void) cached_has_bits;

  // string from_user_id = 1;
  if (this->from_user_id().size() > 0) {
    ::PROTOBUF_NAMESPACE_ID::internal::WireFormatLite::VerifyUtf8String(
      this->_internal_from_user_id().data(), static_cast<int>(this->_internal_from_user_id().length()),
      ::PROTOBUF_NAMESPACE_ID::internal::WireFormatLite::SERIALIZE,
      "IM.SwitchService.IMP2PCmdMsg.from_user_id");
    target = stream->WriteStringMaybeAliased(
        1, this->_internal_from_user_id(), target);
  }

  // string to_user_id = 2;
  if (this->to_user_id().size() > 0) {
    ::PROTOBUF_NAMESPACE_ID::internal::WireFormatLite::VerifyUtf8String(
      this->_internal_to_user_id().data(), static_cast<int>(this->_internal_to_user_id().length()),
      ::PROTOBUF_NAMESPACE_ID::internal::WireFormatLite::SERIALIZE,
      "IM.SwitchService.IMP2PCmdMsg.to_user_id");
    target = stream->WriteStringMaybeAliased(
        2, this->_internal_to_user_id(), target);
  }

  // string cmd_msg_data = 3;
  if (this->cmd_msg_data().size() > 0) {
    ::PROTOBUF_NAMESPACE_ID::internal::WireFormatLite::VerifyUtf8String(
      this->_internal_cmd_msg_data().data(), static_cast<int>(this->_internal_cmd_msg_data().length()),
      ::PROTOBUF_NAMESPACE_ID::internal::WireFormatLite::SERIALIZE,
      "IM.SwitchService.IMP2PCmdMsg.cmd_msg_data");
    target = stream->WriteStringMaybeAliased(
        3, this->_internal_cmd_msg_data(), target);
  }

  if (PROTOBUF_PREDICT_FALSE(_internal_metadata_.have_unknown_fields())) {
    target = stream->WriteRaw(_internal_metadata_.unknown_fields<std::string>(::PROTOBUF_NAMESPACE_ID::internal::GetEmptyString).data(),
        static_cast<int>(_internal_metadata_.unknown_fields<std::string>(::PROTOBUF_NAMESPACE_ID::internal::GetEmptyString).size()), target);
  }
  // @@protoc_insertion_point(serialize_to_array_end:IM.SwitchService.IMP2PCmdMsg)
  return target;
}

size_t IMP2PCmdMsg::ByteSizeLong() const {
// @@protoc_insertion_point(message_byte_size_start:IM.SwitchService.IMP2PCmdMsg)
  size_t total_size = 0;

  ::PROTOBUF_NAMESPACE_ID::uint32 cached_has_bits = 0;
  // Prevent compiler warnings about cached_has_bits being unused
  (void) cached_has_bits;

  // string from_user_id = 1;
  if (this->from_user_id().size() > 0) {
    total_size += 1 +
      ::PROTOBUF_NAMESPACE_ID::internal::WireFormatLite::StringSize(
        this->_internal_from_user_id());
  }

  // string to_user_id = 2;
  if (this->to_user_id().size() > 0) {
    total_size += 1 +
      ::PROTOBUF_NAMESPACE_ID::internal::WireFormatLite::StringSize(
        this->_internal_to_user_id());
  }

  // string cmd_msg_data = 3;
  if (this->cmd_msg_data().size() > 0) {
    total_size += 1 +
      ::PROTOBUF_NAMESPACE_ID::internal::WireFormatLite::StringSize(
        this->_internal_cmd_msg_data());
  }

  if (PROTOBUF_PREDICT_FALSE(_internal_metadata_.have_unknown_fields())) {
    total_size += _internal_metadata_.unknown_fields<std::string>(::PROTOBUF_NAMESPACE_ID::internal::GetEmptyString).size();
  }
  int cached_size = ::PROTOBUF_NAMESPACE_ID::internal::ToCachedSize(total_size);
  SetCachedSize(cached_size);
  return total_size;
}

void IMP2PCmdMsg::CheckTypeAndMergeFrom(
    const ::PROTOBUF_NAMESPACE_ID::MessageLite& from) {
  MergeFrom(*::PROTOBUF_NAMESPACE_ID::internal::DownCast<const IMP2PCmdMsg*>(
      &from));
}

void IMP2PCmdMsg::MergeFrom(const IMP2PCmdMsg& from) {
// @@protoc_insertion_point(class_specific_merge_from_start:IM.SwitchService.IMP2PCmdMsg)
  GOOGLE_DCHECK_NE(&from, this);
  _internal_metadata_.MergeFrom<std::string>(from._internal_metadata_);
  ::PROTOBUF_NAMESPACE_ID::uint32 cached_has_bits = 0;
  (void) cached_has_bits;

  if (from.from_user_id().size() > 0) {
    _internal_set_from_user_id(from._internal_from_user_id());
  }
  if (from.to_user_id().size() > 0) {
    _internal_set_to_user_id(from._internal_to_user_id());
  }
  if (from.cmd_msg_data().size() > 0) {
    _internal_set_cmd_msg_data(from._internal_cmd_msg_data());
  }
}

void IMP2PCmdMsg::CopyFrom(const IMP2PCmdMsg& from) {
// @@protoc_insertion_point(class_specific_copy_from_start:IM.SwitchService.IMP2PCmdMsg)
  if (&from == this) return;
  Clear();
  MergeFrom(from);
}

bool IMP2PCmdMsg::IsInitialized() const {
  return true;
}

void IMP2PCmdMsg::InternalSwap(IMP2PCmdMsg* other) {
  using std::swap;
  _internal_metadata_.Swap<std::string>(&other->_internal_metadata_);
  from_user_id_.Swap(&other->from_user_id_, &::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), GetArena());
  to_user_id_.Swap(&other->to_user_id_, &::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), GetArena());
  cmd_msg_data_.Swap(&other->cmd_msg_data_, &::PROTOBUF_NAMESPACE_ID::internal::GetEmptyStringAlreadyInited(), GetArena());
}

std::string IMP2PCmdMsg::GetTypeName() const {
  return "IM.SwitchService.IMP2PCmdMsg";
}


// @@protoc_insertion_point(namespace_scope)
}  // namespace SwitchService
}  // namespace IM
PROTOBUF_NAMESPACE_OPEN
template<> PROTOBUF_NOINLINE ::IM::SwitchService::IMP2PCmdMsg* Arena::CreateMaybeMessage< ::IM::SwitchService::IMP2PCmdMsg >(Arena* arena) {
  return Arena::CreateMessageInternal< ::IM::SwitchService::IMP2PCmdMsg >(arena);
}
PROTOBUF_NAMESPACE_CLOSE

// @@protoc_insertion_point(global_scope)
#include <google/protobuf/port_undef.inc>
