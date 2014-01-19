# Inherit device configuration
$(call inherit-product, device/lge/p769/full_p769.mk)

# Inherit GSM common stuff
$(call inherit-product, vendor/aokp/configs/gsm.mk)

# Inherit common product files.
$(call inherit-product, vendor/aokp/configs/common.mk)

# boot animation
PRODUCT_COPY_FILES += \
    vendor/aokp/prebuilt/bootanimation/bootanimation_540_960.zip:system/media/bootanimation-alt.zip

PRODUCT_BUILD_PROP_OVERRIDES += BUILD_UTC_DATE=0
PRODUCT_NAME := aokp_p769
PRODUCT_DEVICE := p769
PRODUCT_BRAND := lge
PRODUCT_MODEL := LG-P769
PRODUCT_MANUFACTURER := LGE
