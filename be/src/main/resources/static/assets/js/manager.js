const API_BASE_URL_Mana = 'http://localhost:8080/onboarding';
const AUTH_HEADER_Mana = { 'Authorization': `Bearer ${localStorage.getItem('authToken')}` };
function loadUserDetails(userId) {
    fetch(`${API_BASE_URL}/users/${userId}`, {
        method: 'GET',
        headers: AUTH_HEADER_Mana
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
                document.getElementById('userPosition').innerText = 'Vị trí: ' + (user.roles[0] || 'Không có thông tin');
                document.getElementById('userBio').innerText = 'Tiểu sử: ' + (user.bio || 'Không có thông tin');
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
} function getGroupDetails(groupId) {
    const url = `http://localhost:8080/api/groups/${groupId}`;

    return fetch(url, {
        method: 'GET',
        headers: AUTH_HEADER_Mana,
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
        headers: AUTH_HEADER_Mana, // Thêm header xác thực nếu cần
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
        headers: AUTH_HEADER_Mana, // Thêm header xác thực nếu cần
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



document.getElementById('addManager').addEventListener('click', function () {
    $('#addManagerModal').modal('show');  // Mở modal
    console.log("click");

});

document.getElementById("submitAddManagerButton").addEventListener("click", function () {
    const fullname = document.getElementById('managerFullName').value;
    const email = document.getElementById('managerEmail').value;
    const phone = document.getElementById('managerPhone').value;
    const password = document.getElementById('managerPassword').value;

    if (!fullname || !email || !phone || !password) {
        alert("Please fill in all fields!");
        return;
    }

    if (fullname.length < 3) {
        alert("Full Name must be at least 3 characters long.");
        return;
    }
    const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    if (!emailRegex.test(email)) {
        alert("Please enter a valid email address.");
        return;
    }

    const phoneRegex = /^(\\+84|84|0)(3|5|7|8|9|1[2689])[0-9]{8}$/;
    if (!phoneRegex.test(phone)) {
        alert("Please enter a valid phone number.");
        return;
    }



    const managerData = {
        fullname: fullname,
        email: email,
        phoneNumber: phone,
        password: password
    };

    fetch('http://localhost:8080/onboarding/createManager', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${localStorage.getItem('authToken')}`
        },
        body: JSON.stringify(managerData)
    })
        .then(response => response.json())
        .then(data => {
            if (data.status) {
                alert("Manager created successfully!");
                var addManagerModal = new bootstrap.Modal(document.getElementById("addManagerModal"));
                addManagerModal.hide();
                window.location.reload();
            } else {
                alert("Failed to create manager: " + data.error.message);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('An error occurred while creating the manager.');
        });
});

function checkStatus(userId) {
    const openButton = document.getElementById('openAccount');
    const lockButton = document.getElementById('confirmLock');

    return fetch(`${API_BASE_URL_Mana}/users/${userId}`, {
        method: 'GET',
        headers: AUTH_HEADER_Mana
    })
        .then(res => res.json())
        .then(data => {
            const userAccount = data.data.isLocked; // Lấy trạng thái tài khoản
            if (userAccount === "LOOK") {
                openButton.disabled = false;
                lockButton.disabled = true;
            } else {
                openButton.disabled = true;
                lockButton.disabled = false;
            }
            return userAccount; // Trả về trạng thái tài khoản
        })
        .catch(error => {
            console.error("Có lỗi xảy ra khi lấy thông tin người dùng:", error);
            alert("Có lỗi xảy ra khi lấy thông tin người dùng.");
            throw error; // Quăng lỗi để xử lý bên ngoài
        });
}

document.querySelectorAll('.btn-outline-success').forEach(button => {
    button.addEventListener('click', function () {
        const userId = this.getAttribute('data-id');
        const modal = new bootstrap.Modal(document.getElementById('lockUserModal'));
        modal.show();

        const openButton = document.getElementById('openAccount');
        const lockButton = document.getElementById('confirmLock');

        // Xóa các sự kiện cũ trước khi gắn sự kiện mới
        const newOpenButton = replaceButtonWithClone(openButton);
        const newLockButton = replaceButtonWithClone(lockButton);

        // Kiểm tra trạng thái tài khoản
        checkStatus(userId)
            .then(userAccount => {
                if (userAccount === "LOOK") {
                    newOpenButton.addEventListener("click", () => {
                        fetch(`${API_BASE_URL_Mana}/locked-account/${userId}/isLocked?isLocked=OPEN`, {
                            method: "PUT",
                            headers: AUTH_HEADER_Mana
                        })
                            .then(response => response.json())
                            .then(data => {
                                if (data) {
                                    modal.hide();
                                    alert("OPEN Succesfully")
                                    location.reload();
                                } else {
                                    alert("Có lỗi xảy ra khi mở khóa tài khoản.");
                                }
                            })
                            .catch(error => console.error("Lỗi:", error));
                    });
                } else {
                    newLockButton.addEventListener("click", () => {
                        fetch(`${API_BASE_URL_Mana}/locked-account/${userId}/isLocked?isLocked=LOOK`, {
                            method: "PUT",
                            headers: AUTH_HEADER_Mana
                        })
                            .then(response => response.json())
                            .then(data => {
                                if (data) {
                                    modal.hide();
                                    alert("LOCK Succesfully")
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

//edit manager
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
document.querySelectorAll('.btn-outline-primary').forEach(button => {
    button.addEventListener('click', function () {
        const userId = this.getAttribute('data-id');

        const modal = new bootstrap.Modal(document.getElementById('editManagerModal'));
        modal.show();
        fetch(`http://localhost:8080/onboarding/users/${userId}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('authToken')}`
            }
        })
            .then(res => res.json())
            .then(data => {
                const avatarUrl = data.data.avatarUrl;
                const defaultAvatar = '/assets/img/avatar.jpg'; // Đường dẫn ảnh mặc định
                document.getElementById("editAvatarImgModal").src = avatarUrl ? avatarUrl : defaultAvatar;
                document.getElementById("editPhoneModal").value = data.data.phoneNumber;
                document.getElementById("editBirthDayModalLabel")

            })
    })

})
