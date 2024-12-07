select
from users
where id in (select user_id from participants where id = 'c68ac5ff-e1ff-4a18-af69-601230e9609f')
   or id = '34153e4d-70e4-41a7-a7e8-9a5a850a8b83'

