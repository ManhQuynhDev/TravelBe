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
    const reasonInputLabel = document.getElementById("reasonInputLabel");
    const reasonInput = document.getElementById("reasonInput");
    const unlockConfirmationText = document.getElementById("unlockConfirmationText");

    return fetch(`${API_BASE_URL}/users/${userId}`, {
        method: 'GET',
        headers: AUTH_HEADER
    })
        .then(res => res.json())
        .then(data => {
            const userAccount = data.data.isLocked;

            if (userAccount === "LOCK") {
                // Cập nhật lại tiêu đề và các phần tử hiển thị đúng
                document.getElementById("lockUserModalTitle").innerText = "Mở khóa tài khoản";
                lockButton.style.display = "none"; // Ẩn nút khóa
                openButton.style.display = "block";
                reasonInputLabel.style.display = "none"; // Ẩn lý do khóa
                reasonInput.style.display = "none"; // Ẩn input lý do khóa
                unlockConfirmationText.style.display = "block"; // Hiển thị xác nhận mở khóa
            } else {
                // Cập nhật lại tiêu đề và các phần tử hiển thị đúng
                document.getElementById("lockUserModalTitle").innerText = "Khóa tài khoản";
                openButton.style.display = "none"; // Ẩn nút mở khóa
                lockButton.style.display = "block";
                reasonInputLabel.style.display = "block"; // Hiển thị lý do khóa
                reasonInput.style.display = "block"; // Hiển thị input lý do khóa
                unlockConfirmationText.style.display = "none"; // Ẩn văn bản xác nhận mở khóa
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
                if (userAccount === "LOCK") {
                    newOpenButton.addEventListener("click", () => {
                        fetch(`${API_BASE_URL}/locked-forever/${userId}`, {
                            method: "PUT",
                            headers: {
                                'Authorization': `Bearer ${localStorage.getItem('authToken')}`,
                                'Content-Type': 'application/json',
                            },
                            body: JSON.stringify({
                                isLock: "OPEN",
                                reason: ""
                            })
                        })
                            .then(response => response.json())
                            .then(data => {
                                console.log(data);

                                if (data) {
                                    showNotification('success', 'Thành công', 'Mở tài khoản thành công');
                                    modal.hide();

                                    setTimeout(() => {
                                        window.location.reload();
                                    }, 1000);
                                } else {
                                    showNotification('error', 'Lỗi', 'Có lỗi xảy ra khi mở khóa tài khoản.');
                                }
                            })
                            .catch(error => console.error("Lỗi:", error));
                    });
                } else {
                    newLockButton.addEventListener("click", () => {
                        const reasonInput = document.getElementById("reasonInput").value.trim();

                        if (!reasonInput) {
                            showNotification('warning', 'Cảnh báo', 'Vui lòng nhập lý do khóa tài khoản.');
                            return; // Dừng việc gửi yêu cầu nếu lý do rỗng
                        }

                        fetch(`${API_BASE_URL}/locked-forever/${userId}`, {
                            method: "PUT",
                            headers: {
                                'Authorization': `Bearer ${localStorage.getItem('authToken')}`,
                                'Content-Type': 'application/json',
                            },
                            body: JSON.stringify({
                                isLock: "LOCK",
                                reason: reasonInput // Gửi lý do khóa người dùng
                            })
                        })
                            .then(response => response.json())
                            .then(data => {
                                console.log(data);

                                if (data) {
                                    showNotification('success', 'Thành công', 'Khóa tài khoản thành công');
                                    modal.hide();

                                    setTimeout(() => {
                                        window.location.reload();
                                    }, 1000);
                                } else {
                                    showNotification('error', 'Lỗi', 'Có lỗi xảy ra khi khóa tài khoản.');
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

function showNotification(type, title, message) {
    document.getElementById('notificationModalLabel').textContent = title;
    document.getElementById('notificationModalBody').textContent = message;

    const modalElement = document.getElementById('notificationModal');
    modalElement.classList.remove('modal-success', 'modal-info', 'modal-warning', 'modal-danger');

    if (type === 'success') {
        modalElement.classList.add('modal-success');
    } else if (type === 'info') {
        modalElement.classList.add('modal-info');
    } else if (type === 'warning') {
        modalElement.classList.add('modal-warning');
    } else if (type === 'danger') {
        modalElement.classList.add('modal-danger');
    }

    const modal = new bootstrap.Modal(modalElement);
    modal.show();
}



function replaceButtonWithClone(button) {
    const newButton = button.cloneNode(true);
    button.parentNode.replaceChild(newButton, button);
    return newButton;
}
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






