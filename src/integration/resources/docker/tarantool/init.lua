box.cfg{}

s = box.space.user
if not s then
    s = box.schema.space.create('user')

    p = s:create_index('primary')
end

l = box.space.log_entry
if not l then
    l = box.schema.space.create('log_entry')

    p = l:create_index('primary', {parts = {1, 'string'}})
end