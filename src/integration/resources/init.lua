s = box.space.user
if not s then
    s = box.schema.space.create('user')

    p = s:create_index('primary')

    n = s:create_index('name_index', {parts = {2, 'string'}, unique = false})
end

l = box.space.log_entry
if not l then
    l = box.schema.space.create('log_entry')

    p = l:create_index('primary', {parts = {1, 'string'}})
end

a = box.space.address
if not a then
    a = box.schema.space.create('address')

    address_primary = a:create_index('primary')
end

counter = box.space.counter
if not counter then
    counter = box.schema.space.create('counter')

    counter_primary = counter:create_index('primary')
end