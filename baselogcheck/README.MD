1. 添加LogRecyclerView到需要显示的位置
2. 设置logrv_filter_category和logrv_filter_tag属性
3. category不能为空
4. LogRecyclerView里的run方法启动显示日志
5. 在fragment或者activity中,使用扩展方法logCheckDebugEx就可以决定是否启用日志显示
6. 5秒内点击recyclerView超过23次,可清空日志
7. 摇一摇可隐藏或显示日志