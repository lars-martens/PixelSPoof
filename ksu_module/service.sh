#!/system/bin/sh
# Pixel 10 Pro XL Spoofing Module for KernelSU
# This module modifies system properties to make the device appear as Pixel 10 Pro XL

MODDIR=${0%/*}
LOG_FILE="/data/local/tmp/pixel_spoof.log"

log_print() {
    echo "[$(date)] $1" >> $LOG_FILE
    echo "$1"
}

log_print "Starting Pixel 10 Pro XL spoofing..."

# Wait for system to be ready
sleep 5

# Reset system properties to Pixel 10 Pro XL
resetprop_if_exist() {
    local prop_name="$1"
    local prop_value="$2"
    
    if [ -n "$(getprop $prop_name)" ]; then
        resetprop "$prop_name" "$prop_value"
        log_print "Set $prop_name = $prop_value"
    fi
}

# Core device identification
resetprop_if_exist "ro.product.manufacturer" "Google"
resetprop_if_exist "ro.product.brand" "google" 
resetprop_if_exist "ro.product.name" "mustang"
resetprop_if_exist "ro.product.device" "mustang"
resetprop_if_exist "ro.product.model" "Pixel 10 Pro XL"
resetprop_if_exist "ro.product.product.manufacturer" "Google"
resetprop_if_exist "ro.product.product.brand" "google"
resetprop_if_exist "ro.product.product.name" "mustang"
resetprop_if_exist "ro.product.product.device" "mustang"
resetprop_if_exist "ro.product.product.model" "Pixel 10 Pro XL"

# Build properties - Android 16 BP2A.250805.005
resetprop_if_exist "ro.build.product" "mustang"
resetprop_if_exist "ro.build.device" "mustang"
resetprop_if_exist "ro.build.fingerprint" "google/mustang/mustang:16/BP2A.250805.005/13691446:user/release-keys"
resetprop_if_exist "ro.build.description" "mustang-user 16 BP2A.250805.005 13691446 release-keys"
resetprop_if_exist "ro.build.id" "BP2A.250805.005"
resetprop_if_exist "ro.build.display.id" "BP2A.250805.005"
resetprop_if_exist "ro.build.version.release" "16"
resetprop_if_exist "ro.build.version.release_or_codename" "16"
resetprop_if_exist "ro.build.version.sdk" "36"
resetprop_if_exist "ro.build.version.incremental" "13691446"
resetprop_if_exist "ro.build.version.security_patch" "2025-08-05"
resetprop_if_exist "ro.build.tags" "release-keys"
resetprop_if_exist "ro.build.type" "user"
resetprop_if_exist "ro.build.host" "e27561acca81"
resetprop_if_exist "ro.build.user" "android-build"
resetprop_if_exist "ro.build.flavor" "mustang-user"

# Hardware properties for Pixel 10 Pro XL (Tensor G5)
resetprop_if_exist "ro.hardware" "mustang"
resetprop_if_exist "ro.board.platform" "gs102"
resetprop_if_exist "ro.hardware.chipname" "Tensor G5"
resetprop_if_exist "ro.soc.manufacturer" "Google"
resetprop_if_exist "ro.soc.model" "Tensor G5"
resetprop_if_exist "ro.hardware.platform" "gs102"
resetprop_if_exist "ro.product.board" "mustang"

# CPU Architecture
resetprop_if_exist "ro.product.cpu.abi" "arm64-v8a"
resetprop_if_exist "ro.product.cpu.abilist" "arm64-v8a,armeabi-v7a,armeabi"
resetprop_if_exist "ro.product.cpu.abilist32" "armeabi-v7a,armeabi"
resetprop_if_exist "ro.product.cpu.abilist64" "arm64-v8a"
resetprop_if_exist "ro.arch" "arm64"
resetprop_if_exist "ro.cpu.architecture" "arm64"

# Processor details
resetprop_if_exist "ro.processor.model" "Google Tensor G5"
resetprop_if_exist "ro.processor.manufacturer" "Google"
resetprop_if_exist "ro.cpu.model" "Google Tensor G5"
resetprop_if_exist "ro.cpu.vendor" "Google"
resetprop_if_exist "ro.cpu.cores" "8"

# GPU details
resetprop_if_exist "ro.gpu.model" "Mali-G78 MP24"
resetprop_if_exist "ro.gpu.manufacturer" "ARM"
resetprop_if_exist "ro.gpu.vendor" "ARM"
resetprop_if_exist "ro.gpu.cores" "24"

# Display properties
resetprop_if_exist "ro.display.density" "560"
resetprop_if_exist "ro.display.size" "6.8"

# Bootloader and security
resetprop_if_exist "ro.bootloader" "mustang-16.0-13691446"
resetprop_if_exist "ro.boot.hardware" "mustang"
resetprop_if_exist "ro.boot.hardware.platform" "gs102"
resetprop_if_exist "ro.boot.hardware.revision" "PROTO1.0"
resetprop_if_exist "ro.boot.hardware.sku" "mustang"

# Hardware SKUs
resetprop_if_exist "ro.build.hardware.sku" "mustang"
resetprop_if_exist "ro.product.hardware.sku" "mustang"
resetprop_if_exist "ro.product.hardware.platform" "gs102"

# Additional Pixel-specific properties
resetprop_if_exist "ro.opa.eligible_device" "true"
resetprop_if_exist "ro.com.google.ime.kb_pad_port_b" "1"
resetprop_if_exist "ro.storage_manager.enabled" "true"
resetprop_if_exist "ro.atrace.core.services" "com.google.android.gms,com.google.android.gms.ui,com.google.android.gms.persistent"

# AICore properties - CRUCIAL for Pixel AI features
resetprop_if_exist "ro.config.aicore_enabled" "true"
resetprop_if_exist "persist.vendor.aicore.enabled" "1"
resetprop_if_exist "ro.vendor.aicore.version" "16.0"
resetprop_if_exist "ro.system.aicore.enabled" "true"
resetprop_if_exist "ro.config.ai_core_available" "true"

# Pixel exclusive features
resetprop_if_exist "ro.vendor.audio.sdk.fluencetype" "fluence"
resetprop_if_exist "ro.config.face_unlock_service" "true"
resetprop_if_exist "ro.com.google.lens.oem" "true"
resetprop_if_exist "ro.support_one_handed_mode" "true"
resetprop_if_exist "ro.quick_start.device_id" "mustang"
resetprop_if_exist "ro.hotword.detection_service_required" "false"

# Additional commonly queried properties for device info apps
resetprop_if_exist "ro.chipname" "Tensor G5"
resetprop_if_exist "ro.hardware.chipset" "Tensor G5"
resetprop_if_exist "ro.soc.vendor" "Google"
resetprop_if_exist "ro.vendor.name" "google"
resetprop_if_exist "ro.vendor.product.name" "mustang"
resetprop_if_exist "ro.vendor.product.device" "mustang"
resetprop_if_exist "ro.vendor.product.model" "Pixel 10 Pro XL"

# Version codenames
resetprop_if_exist "ro.build.version.codename" "REL"
resetprop_if_exist "ro.build.version.all_codenames" "REL"
resetprop_if_exist "ro.build.version.preview_sdk" "0"
resetprop_if_exist "ro.build.version.preview_sdk_fingerprint" "REL"

# Serial numbers
resetprop_if_exist "ro.serialno" "HT7A1TESTDEVICE"
resetprop_if_exist "ro.boot.serialno" "HT7A1TESTDEVICE"
resetprop_if_exist "ril.serial_number" "HT7A1TESTDEVICE"

log_print "Pixel 10 Pro XL spoofing completed successfully!"

# Create spoofed /proc/cpuinfo for hardware detection bypass
log_print "Setting up CPU info spoofing..."
CPUINFO_SPOOF="/data/local/tmp/cpuinfo_spoof"
cat > $CPUINFO_SPOOF << 'EOF'
processor	: 0
BogoMIPS	: 38.40
Features	: fp asimd evtstrm aes pmull sha1 sha2 crc32 atomics fphp asimdhp cpuid asimdrdm jscvt fcma lrcpc dcpop sha3 sm3 sm4 asimddp sha512 sve asimdfhm dit uscat ilrcpc flagm ssbs paca pacg dcpodp sve2 sveaes svepmull svebitperm svesha3 svesm4 flagm2 frint svei8mm svebf16 i8mm bf16 dgh bti
CPU implementer	: 0x41
CPU architecture: 8
CPU variant	: 0x3
CPU part	: 0xd0c
CPU revision	: 1

processor	: 1
BogoMIPS	: 38.40
Features	: fp asimd evtstrm aes pmull sha1 sha2 crc32 atomics fphp asimdhp cpuid asimdrdm jscvt fcma lrcpc dcpop sha3 sm3 sm4 asimddp sha512 sve asimdfhm dit uscat ilrcpc flagm ssbs paca pacg dcpodp sve2 sveaes svepmull svebitperm svesha3 svesm4 flagm2 frint svei8mm svebf16 i8mm bf16 dgh bti
CPU implementer	: 0x41
CPU architecture: 8
CPU variant	: 0x3
CPU part	: 0xd0c
CPU revision	: 1

processor	: 2
BogoMIPS	: 38.40
Features	: fp asimd evtstrm aes pmull sha1 sha2 crc32 atomics fphp asimdhp cpuid asimdrdm jscvt fcma lrcpc dcpop sha3 sm3 sm4 asimddp sha512 sve asimdfhm dit uscat ilrcpc flagm ssbs paca pacg dcpodp sve2 sveaes svepmull svebitperm svesha3 svesm4 flagm2 frint svei8mm svebf16 i8mm bf16 dgh bti
CPU implementer	: 0x41
CPU architecture: 8
CPU variant	: 0x3
CPU part	: 0xd0c
CPU revision	: 1

processor	: 3
BogoMIPS	: 38.40
Features	: fp asimd evtstrm aes pmull sha1 sha2 crc32 atomics fphp asimdhp cpuid asimdrdm jscvt fcma lrcpc dcpop sha3 sm3 sm4 asimddp sha512 sve asimdfhm dit uscat ilrcpc flagm ssbs paca pacg dcpodp sve2 sveaes svepmull svebitperm svesha3 svesm4 flagm2 frint svei8mm svebf16 i8mm bf16 dgh bti
CPU implementer	: 0x41
CPU architecture: 8
CPU variant	: 0x3
CPU part	: 0xd0c
CPU revision	: 1

processor	: 4
BogoMIPS	: 38.40
Features	: fp asimd evtstrm aes pmull sha1 sha2 crc32 atomics fphp asimdhp cpuid asimdrdm jscvt fcma lrcpc dcpop sha3 sm3 sm4 asimddp sha512 sve asimdfhm dit uscat ilrcpc flagm ssbs paca pacg dcpodp sve2 sveaes svepmull svebitperm svesha3 svesm4 flagm2 frint svei8mm svebf16 i8mm bf16 dgh bti
CPU implementer	: 0x41
CPU architecture: 8
CPU variant	: 0x3
CPU part	: 0xd0c
CPU revision	: 1

processor	: 5
BogoMIPS	: 38.40
Features	: fp asimd evtstrm aes pmull sha1 sha2 crc32 atomics fphp asimdhp cpuid asimdrdm jscvt fcma lrcpc dcpop sha3 sm3 sm4 asimddp sha512 sve asimdfhm dit uscat ilrcpc flagm ssbs paca pacg dcpodp sve2 sveaes svepmull svebitperm svesha3 svesm4 flagm2 frint svei8mm svebf16 i8mm bf16 dgh bti
CPU implementer	: 0x41
CPU architecture: 8
CPU variant	: 0x3
CPU part	: 0xd0c
CPU revision	: 1

processor	: 6
BogoMIPS	: 38.40
Features	: fp asimd evtstrm aes pmull sha1 sha2 crc32 atomics fphp asimdhp cpuid asimdrdm jscvt fcma lrcpc dcpop sha3 sm3 sm4 asimddp sha512 sve asimdfhm dit uscat ilrcpc flagm ssbs paca pacg dcpodp sve2 sveaes svepmull svebitperm svesha3 svesm4 flagm2 frint svei8mm svebf16 i8mm bf16 dgh bti
CPU implementer	: 0x41
CPU architecture: 8
CPU variant	: 0x3
CPU part	: 0xd0c
CPU revision	: 1

processor	: 7
BogoMIPS	: 38.40
Features	: fp asimd evtstrm aes pmull sha1 sha2 crc32 atomics fphp asimdhp cpuid asimdrdm jscvt fcma lrcpc dcpop sha3 sm3 sm4 asimddp sha512 sve asimdfhm dit uscat ilrcpc flagm ssbs paca pacg dcpodp sve2 sveaes svepmull svebitperm svesha3 svesm4 flagm2 frint svei8mm svebf16 i8mm bf16 dgh bti
CPU implementer	: 0x41
CPU architecture: 8
CPU variant	: 0x3
CPU part	: 0xd0c
CPU revision	: 1

processor	: 8
BogoMIPS	: 38.40
Features	: fp asimd evtstrm aes pmull sha1 sha2 crc32 atomics fphp asimdhp cpuid asimdrdm jscvt fcma lrcpc dcpop sha3 sm3 sm4 asimddp sha512 sve asimdfhm dit uscat ilrcpc flagm ssbs paca pacg dcpodp sve2 sveaes svepmull svebitperm svesha3 svesm4 flagm2 frint svei8mm svebf16 i8mm bf16 dgh bti
CPU implementer	: 0x41
CPU architecture: 8
CPU variant	: 0x3
CPU part	: 0xd0c
CPU revision	: 1

Hardware	: mustang
Revision	: 0000
Serial		: 0000000000000000
EOF

# Backup original cpuinfo and mount spoofed version
if [ ! -f /proc/cpuinfo.backup ]; then
    cp /proc/cpuinfo /proc/cpuinfo.backup
    log_print "Backed up original /proc/cpuinfo"
fi

# Use bind mount to replace cpuinfo - DISABLED FOR SAFETY
# mount -o bind $CPUINFO_SPOOF /proc/cpuinfo
# if [ $? -eq 0 ]; then
#     log_print "Successfully mounted spoofed /proc/cpuinfo"
# else
#     log_print "Failed to mount spoofed /proc/cpuinfo"
# fi
log_print "CPU info spoofing DISABLED - causing bootloop"

# Verify some key properties
log_print "Verification:"
log_print "Manufacturer: $(getprop ro.product.manufacturer)"
log_print "Model: $(getprop ro.product.model)" 
log_print "Brand: $(getprop ro.product.brand)"
log_print "Fingerprint: $(getprop ro.build.fingerprint)"
