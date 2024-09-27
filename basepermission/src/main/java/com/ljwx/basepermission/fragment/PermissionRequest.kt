package com.ljwx.basepermission.fragment


class PermissionRequest(
    var mPermissions: MutableList<String>,
    var mCallBacks: MutableList<IPermissionCallBack>?,
    var mRationale: String?,
    var mShouldShowRationale: Boolean
)