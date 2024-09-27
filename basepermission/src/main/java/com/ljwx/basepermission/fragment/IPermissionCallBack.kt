package com.ljwx.basepermission.fragment

interface IPermissionCallBack {
    fun onPermissionsGranted(grantedAll: Boolean, perms: List<String>)
}