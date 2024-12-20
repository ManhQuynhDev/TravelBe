$(document).on('ready', function () {
    if (window.localStorage.getItem('hs-builder-popover') === null) {
        $('#builderPopover').popover('show')
            .on('shown.bs.popover', function () {
                $('.popover').last().addClass('popover-dark')
            });

        $(document).on('click', '#closeBuilderPopover', function () {
            window.localStorage.setItem('hs-builder-popover', true);
            $('#builderPopover').popover('dispose');
        });
    } else {
        $('#builderPopover').on('show.bs.popover', function () {
            return false
        });
    }


});

const API_BASE_URL = 'http://localhost:8080/onboarding';
const AUTH_HEADER = { 'Authorization': `Bearer ${localStorage.getItem('authToken')}` };

document.getElementById('editAvatarUploaderModal').addEventListener('change', function (event) {
    const file = event.target.files[0];
    if (file) {
        const reader = new FileReader();
        reader.onload = function (e) {
            const avatarImage = document.getElementById('editAvatarImgModal');
            avatarImage.src = e.target.result;  // Thay đổi src của hình ảnh
        };
        reader.readAsDataURL(file);
    }
});

function checkStatus(userId) {
    const openButton = document.getElementById('openAccount');
    const lockButton = document.getElementById('confirmLock');

    return fetch(`${API_BASE_URL}/users/${userId}`, {
        method: 'GET',
        headers: AUTH_HEADER
    })
        .then(res => res.json())
        .then(data => {
            const userAccount = data.data.isLocked;
            if (userAccount === "LOOK") {
                openButton.disabled = false;
                lockButton.disabled = true;
            } else {
                openButton.disabled = true;
                lockButton.disabled = false;
            }
            return userAccount;
        })
        .catch(error => {
            console.error("Có lỗi xảy ra khi lấy thông tin người dùng:", error);
            alert("Có lỗi xảy ra khi lấy thông tin người dùng.");
            throw error;
        });
}

document.querySelectorAll('.btn-outline-success').forEach(button => {
    button.addEventListener('click', function () {
        const userId = this.getAttribute('data-id');
        const modal = new bootstrap.Modal(document.getElementById('lockUserModal'));
        modal.show();

        const openButton = document.getElementById('openAccount');
        const lockButton = document.getElementById('confirmLock');

        const newOpenButton = replaceButtonWithClone(openButton);
        const newLockButton = replaceButtonWithClone(lockButton);

        checkStatus(userId)
            .then(userAccount => {
                if (userAccount === "LOOK") {
                    newOpenButton.addEventListener("click", () => {
                        fetch(`${API_BASE_URL}/locked-account/${userId}/isLocked?isLocked=OPEN`, {
                            method: "PUT",
                            headers: AUTH_HEADER
                        })
                            .then(response => response.json())
                            .then(data => {
                                if (data) {
                                    modal.hide();
                                    alert("Mở tài khoản thành công")
                                    location.reload();
                                } else {
                                    alert("Có lỗi xảy ra khi mở khóa tài khoản.");
                                }
                            })
                            .catch(error => console.error("Lỗi:", error));
                    });
                } else {
                    newLockButton.addEventListener("click", () => {
                        fetch(`${API_BASE_URL}/locked-account/${userId}/isLocked?isLocked=LOOK`, {
                            method: "PUT",
                            headers: AUTH_HEADER
                        })
                            .then(response => response.json())
                            .then(data => {
                                if (data) {
                                    modal.hide();
                                    alert("Khóa tài khoản thành công")
                                    location.reload();
                                } else {
                                    alert("Có lỗi xảy ra khi khóa tài khoản.");
                                }
                            })
                            .catch(error => console.error("Lỗi:", error));
                    });
                }
            })
            .catch(error => {
                console.error("Không thể kiểm tra trạng thái tài khoản:", error);
            });
    });
});

function replaceButtonWithClone(button) {
    const newButton = button.cloneNode(true);
    button.parentNode.replaceChild(newButton, button);
    return newButton;
}



document.getElementById("addUser").addEventListener("click", function () {
    const modal = new bootstrap.Modal(document.getElementById('addUserModal'));
    modal.show();
});

document.getElementById("submitAddUserButton").addEventListener("click", () => {
    // Vô hiệu hóa nút submit để ngừng người dùng nhấn nhiều lần
    const submitButton = document.getElementById("submitAddUserButton");
    submitButton.disabled = true;

    const fullname = document.getElementById('managerFullName').value.trim();
    const email = document.getElementById('managerEmail').value.trim();
    const phone = document.getElementById('managerPhone').value.trim();
    const password = document.getElementById('managerPassword').value.trim();

    if (!fullname || !email || !phone || !password) {
        alert("Vui lòng điền đầy đủ thông tin!");
        submitButton.disabled = false;  // Kích hoạt lại nút submit
        return;
    }

    if (fullname.length < 3) {
        alert("Họ tên phải có ít nhất 3 ký tự.");
        submitButton.disabled = false;  // Kích hoạt lại nút submit
        return;
    }

    // Kiểm tra tên có chứa ký tự đặc biệt ngoài dấu cách không
    if (/[^A-Za-zÀÁÂÃÈÉÊÌÍÒÓÔÕÙÚĂĐĨŨƠàáâãèéêìíòóôõùúăđĩũơƯĂÂÊÔƠƯ áàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵ\s]/.test(fullname)) {
        alert("Tên không được chứa ký tự đặc biệt ngoài dấu cách!");
        submitButton.disabled = false;  // Kích hoạt lại nút submit
        return;
    }

    const emailRegex = /^[a-zA-Z][a-zA-Z0-9_!#$%&'*+/=?`{|}~^-]*(?:\.[a-zA-Z0-9_!#$%&'*+/=?`{|}~^-]+)*@[a-zA-Z0-9-]+\.[a-zA-Z]{2,}$/;
    if (!emailRegex.test(email)) {
        alert("Vui lòng nhập địa chỉ email hợp lệ.");
        submitButton.disabled = false;  // Kích hoạt lại nút submit
        return;
    }

    const phoneRegex = /^(\\+84|84|0)(3|5|7|8|9|1[2689])[0-9]{8}$/;
    if (!phoneRegex.test(phone)) {
        alert("Vui lòng nhập số điện thoại hợp lệ.");
        submitButton.disabled = false;  // Kích hoạt lại nút submit
        return;
    }
    const passwordRegexLowercase = /[a-z]/;
    const passwordRegexUppercase = /[A-Z]/;
    const passwordRegexNumber = /\d/;
    const passwordRegexSpecialChar = /[@$!%*?&]/;
    const passwordMinLength = 8;

    if (password.length < passwordMinLength) {
        alert("Mật khẩu phải có ít nhất 8 ký tự.");
        submitButton.disabled = false;  // Kích hoạt lại nút submit
        return;
    }
    if (!passwordRegexLowercase.test(password)) {
        alert("Mật khẩu phải có ít nhất 1 chữ cái viết thường.");
        submitButton.disabled = false;  // Kích hoạt lại nút submit
        return;
    }
    if (!passwordRegexUppercase.test(password)) {
        alert("Mật khẩu phải có ít nhất 1 chữ cái viết hoa.");
        submitButton.disabled = false;  // Kích hoạt lại nút submit
        return;
    }
    if (!passwordRegexNumber.test(password)) {
        alert("Mật khẩu phải có ít nhất 1 chữ số.");
        submitButton.disabled = false;  // Kích hoạt lại nút submit
        return;
    }
    if (!passwordRegexSpecialChar.test(password)) {
        alert("Mật khẩu phải có ít nhất 1 ký tự đặc biệt.");
        submitButton.disabled = false;  // Kích hoạt lại nút submit
        return;
    }

    const userData = {
        fullname: fullname,
        email: email,
        phoneNumber: phone,
        password: password
    };

    fetch('http://localhost:8080/onboarding/register', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${localStorage.getItem('authToken')}`
        },
        body: JSON.stringify(userData)
    })
        .then(response => response.json())
        .then(data => {
            if (data.status) {
                alert("Tạo người dùng thành công!");
                var addManagerModal = new bootstrap.Modal(document.getElementById("addUserModal"));
                addManagerModal.hide();
                window.location.reload();
            } else {
                if (data.error && data.error.message.includes("already exist")) {
                    alert("Email đã tồn tại. Vui lòng thử email khác.");
                } else {
                    alert("Tạo người dùng thất bại: " + data.error.message);
                }
            }
            // Kích hoạt lại nút submit sau khi hoàn tất
            submitButton.disabled = false;
        })
        .catch(error => {
            console.error('Lỗi:', error);
            alert('Đã xảy ra lỗi khi tạo người dùng.');
            submitButton.disabled = false;  // Kích hoạt lại nút submit
        });
});



function loadUserDetails(userId) {
    fetch(`${API_BASE_URL}/users/${userId}`, {
        method: 'GET',
        headers: AUTH_HEADER
    })
        .then(response => response.json())
        .then(data => {
            if (data.status) {
                const user = data.data;
                console.log(user);
                console.log(user.fullname);

                const avatarUrl = user.avatarUrl || '';
                if (avatarUrl) {
                    document.getElementById('userAvatarImgModal').src = avatarUrl;
                    document.getElementById('userAvatarImgModal').style.display = 'block';
                    document.getElementById('userAvatarTextModal').style.display = 'none';
                } else {
                    const firstLetter = user.fullname ? user.fullname.charAt(0).toUpperCase() : 'N';
                    document.getElementById('avatarInitial').innerText = firstLetter;
                    document.getElementById('userAvatarTextModal').style.display = 'flex';
                    document.getElementById('userAvatarImgModal').style.display = 'none';
                }
                document.getElementById('managerFullNameDetails').innerText = user.fullname;
                document.getElementById('managerRolesDetails').innerText = user.roles[0];
                const date = new Date(user.create_at);
                const year = date.getFullYear();
                const month = String(date.getMonth() + 1).padStart(2, '0');
                document.getElementById("managerCreateTimeDetails").innerText = `Tham gia ${year} - ${month}`;

                document.getElementById('userFullNamePro').innerText = 'Họ tên: ' + (user.fullname || 'Không có thông tin');
                document.getElementById('userPosition').innerText = 'Chức vụ: ' + (user.roles[0] || 'Không có thông tin');
                document.getElementById('userBio').innerText = 'Giới thiệu: ' + (user.bio || 'Không có thông tin');
                document.getElementById('userDOB').innerText = 'Ngày sinh: ' + (user.dob || 'Không có thông tin');

                // Hiển thị modal
                const modal = new bootstrap.Modal(document.getElementById('userDetailModal'));
                modal.show();

                joinedGroup(userId);
                userCreatedGroups(userId);
                getFriendsList(userId);

            } else {
                console.error('Lỗi khi tải thông tin người dùng');
            }
        })
        .catch(error => {
            console.error('Lỗi mạng:', error);
        });
}


document.getElementById("logoutLink").addEventListener("click", function () {
    window.location.replace("/web-server/login");
});


// Hàm lấy tên nhóm theo groupId
function getGroupDetails(groupId) {
    const url = `http://localhost:8080/api/groups/${groupId}`;

    return fetch(url, {
        method: 'GET',
        headers: AUTH_HEADER,
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`Không thể lấy chi tiết nhóm với ID ${groupId}, Mã trạng thái: ${response.status}`);
            }
            return response.json(); // Chuyển đổi response thành JSON
        })
        .then(data => {
            if (data.status && data.data) {
                return {
                    group_name: data.data.group_name || 'Không có',      // Tên nhóm
                    admin_name: data.data.admin_name || 'Không có',      // Tên admin
                    member_count: data.data.member_count || 0,           // Số lượng thành viên
                };
            } else {
                console.error(`Không thể lấy chi tiết nhóm: ${data.message}`);
                return null;
            }
        })
        .catch(error => {
            console.error("Lỗi khi lấy thông tin nhóm:", error);
            return null;
        });
}


function joinedGroup(userId) {
    const url = `http://localhost:8080/api/member/group-join-id/${userId}`;

    fetch(url, {
        method: 'GET',
        headers: AUTH_HEADER, // Thêm header xác thực nếu cần
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`Không thể lấy danh sách nhóm người dùng, Mã trạng thái: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            const groupIds = data.data; // Mảng các groupId
            const groupTableBody = document.getElementById('joined-groups-table-body');

            groupTableBody.innerHTML = ''; // Xóa các phần tử cũ trước khi thêm mới

            if (!groupIds || groupIds.length === 0) {
                const noGroupsRow = document.createElement('tr');
                noGroupsRow.innerHTML = '<td colspan="4" class="text-center">Người dùng chưa tham gia nhóm nào</td>';
                groupTableBody.appendChild(noGroupsRow);
                return;
            }

            // Lấy chi tiết các nhóm dựa trên groupId
            const groupDetailsPromises = groupIds.map(groupId => getGroupDetails(groupId));

            Promise.all(groupDetailsPromises)
                .then(groupDetails => {
                    groupDetails.forEach((group, index) => {
                        if (group) {
                            const row = document.createElement('tr');
                            row.innerHTML = `
                                <th scope="row">${index + 1}</th>
                                <td>${group.group_name}</td> <!-- Tên nhóm -->
                                <td>${group.admin_name}</td> <!-- Tên admin -->
                                <td>${group.member_count}</td> <!-- Số lượng thành viên -->
                            `;
                            groupTableBody.appendChild(row);
                        }
                    });
                })
                .catch(error => {
                    console.error('Lỗi khi lấy chi tiết nhóm:', error);
                });
        })
        .catch(error => {
            console.error('Lỗi khi lấy danh sách nhóm người dùng:', error);
        });
}



function userCreatedGroups(userId) {
    const url = `http://localhost:8080/api/member/user-create-id/${userId}`;

    fetch(url, {
        method: 'GET',
        headers: AUTH_HEADER, // Thêm header xác thực nếu cần
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`Không thể lấy danh sách nhóm đã tạo, Mã trạng thái: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            const groupIds = data.data; // Mảng groupId
            const groupDetailsPromises = groupIds.map(groupId => getGroupDetails(groupId)); // Gọi API chi tiết group

            Promise.all(groupDetailsPromises)
                .then(groupDetails => {
                    const groupTableBody = document.getElementById('created-groups-table-body');
                    groupTableBody.innerHTML = ''; // Xóa danh sách cũ

                    // Nếu không có nhóm
                    if (!groupDetails || groupDetails.length === 0) {
                        const noGroupsRow = document.createElement('tr');
                        noGroupsRow.innerHTML = '<td colspan="4" class="text-center">Người dùng chưa tạo nhóm nào</td>';
                        groupTableBody.appendChild(noGroupsRow);
                        return;
                    }

                    groupDetails.forEach((group, index) => {
                        if (group) {
                            const row = document.createElement('tr');
                            row.innerHTML = `
                                <th scope="row">${index + 1}</th>
                                <td>${group.group_name}</td> <!-- Tên nhóm -->
                                <td>${group.admin_name}</td> <!-- Tên admin -->
                                <td>${group.member_count}</td> <!-- Số lượng thành viên -->
                            `;
                            groupTableBody.appendChild(row);
                        }
                    });
                })
                .catch(error => {
                    console.error('Lỗi khi lấy chi tiết nhóm:', error);
                });
        })
        .catch(error => {
            console.error('Lỗi khi lấy nhóm đã tạo của người dùng:', error);
        });
}



function getFriendsList(userId) {
    fetch(`http://localhost:8080/api/friend/get-all-friends/${userId}?status=APPROVED&page=0&size=100`, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${localStorage.getItem('authToken')}` // Nếu cần xác thực
        }
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`Không thể lấy danh sách bạn bè: ${response.status}`);
            }
            return response.json();
        })
        .then(friendsData => {
            const friendsList = document.getElementById('friendsList');
            if (friendsData && friendsData.content && friendsData.content.length > 0) {
                friendsList.innerHTML = ''; // Xóa danh sách cũ
                friendsData.content.forEach((friend, index) => {
                    const row = document.createElement('tr');
                    const avatarContent = friend.fullname ? friend.fullname.charAt(0).toUpperCase() : '';
                    row.innerHTML = `
                    <th scope="row">${index + 1}</th>
                    <td class="text-center">
                        ${friend.avatarUrl ?
                            `<img src="${friend.avatarUrl}" alt="Avatar" class="img-fluid rounded-circle" style="width: 40px; height: 40px; background-color: #f0f0f0; color: #555; font-size: 32px; font-weight: bold; display: flex; justify-content: center; align-items: center;">` :
                            `<span class="d-flex justify-content-center align-items-center" style="background-color: #f0f0f0; color: #555; width: 40px; height: 40px; font-size: 20px; font-weight: bold; border-radius: 50%;">${avatarContent}</span>`
                        }
                    </td>
                    <td>${friend.fullname}</td>
                    <td>${friend.status || 'Đang trực tuyến'}</td>
                `;
                    friendsList.appendChild(row);
                });
            } else {
                friendsList.innerHTML = '<tr><td colspan="4" class="text-center">Không có bạn bè nào</td></tr>';
            }
        })
        .catch(error => {
            console.error('Lỗi khi lấy danh sách bạn bè:', error);
            document.getElementById('friendsList').innerHTML = '<tr><td colspan="4" class="text-center">Lỗi khi tải danh sách bạn bè</td></tr>';
        });
}






