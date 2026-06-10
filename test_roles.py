#!/usr/bin/env python3
import json, urllib.request, urllib.error

def post(path, data, headers=None):
    h = {'Content-Type': 'application/json'}
    if headers: h.update(headers)
    req = urllib.request.Request('http://localhost:8080' + path,
                                 data=json.dumps(data).encode(),
                                 headers=h, method='POST')
    try:
        with urllib.request.urlopen(req) as r:
            return r.getcode(), json.loads(r.read())
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode()[:200]

def get(path, headers=None):
    req = urllib.request.Request('http://localhost:8080' + path, headers=headers or {})
    try:
        with urllib.request.urlopen(req) as r:
            return r.getcode(), r.read().decode()[:200]
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode()[:200]

# 登录两个角色
c1, d1 = post('/api/auth/login', {'username': 'student1', 'password': '123456'})
stu = d1['data']['token']
c2, d2 = post('/api/auth/login', {'username': 'admin', 'password': '123456'})
adm = d2['data']['token']
print(f"STU token len: {len(stu)}  ADMIN token len: {len(adm)}")

# 测试
stu_h = {'Authorization': 'Bearer ' + stu}
adm_h = {'Authorization': 'Bearer ' + adm}

print()
print(f"STU  -> GET /api/orders/admin : {get('/api/orders/admin', stu_h)[0]}  (expect 403)")
print(f"ADMIN-> GET /api/orders/admin : {get('/api/orders/admin', adm_h)[0]}  (expect 200)")
print(f"STU  -> POST /api/dishes     : {post('/api/dishes', {}, stu_h)[0]}  (expect 403)")
print(f"ADMIN-> POST /api/dishes     : {post('/api/dishes', {}, adm_h)[0]}  (expect 400 业务校验)")
print(f"no   -> GET /api/dishes/1    : {get('/api/dishes/1')[0]}  (expect 200)")
