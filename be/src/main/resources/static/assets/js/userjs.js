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
                        fetch(`${API_BASE_URL}/locked-account/${userId}/isLocked?isLocked=OPEN`, {
                            method: "PUT",
                            headers: AUTH_HEADER
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
                        fetch(`${API_BASE_URL}/locked-account/${userId}/isLocked?isLocked=LOOK`, {
                            method: "PUT",
                            headers: AUTH_HEADER
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



document.getElementById("addUser").addEventListener("click", function () {
    const modal = new bootstrap.Modal(document.getElementById('addUserModal'));
    modal.show();
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
                    const firstLetter = user.fullname ? user.fullname.charAt(0).toUpperCase() : 'N'; // Lấy chữ cái đầu tiên của fullname
                    document.getElementById('avatarInitial').innerText = firstLetter;
                    document.getElementById('userAvatarTextModal').style.display = 'flex';
                    document.getElementById('userAvatarImgModal').style.display = 'none';
                }
                document.getElementById('managerFullNameDetails').innerText = user.fullname
                document.getElementById('managerRolesDetails').innerText = user.roles[0]
                const date = new Date(user.create_at);
                const year = date.getFullYear();
                const month = String(date.getMonth() + 1).padStart(2, '0'); // Tháng bắt đầu từ 0 nên cần +1
                document.getElementById("managerCreateTimeDetails").innerText = `Joined ${year} -${month}`;

                document.getElementById('userFullNamePro').innerText = 'Full Name: ' + (user.fullname || 'Not Available');
                document.getElementById('userPosition').innerText = 'Role: ' + (user.roles[0] || 'Not Available');
                document.getElementById('userBio').innerText = 'Bio: ' + (user.bio || 'Not Available');
                document.getElementById('userDOB').innerText = 'Date of Birth: ' + (user.dob || 'Not Available');

                // Hiển thị modal
                const modal = new bootstrap.Modal(document.getElementById('userDetailModal'));
                modal.show();

                joinedGroup(userId)
                userCreatedGroups(userId)
                getFriendsList(userId)


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


document.querySelectorAll('.nav-link').forEach(tab => {
    tab.addEventListener('click', function () {
        // Ẩn phần tử profile khi chuyển sang tab khác
        document.querySelector('#profile').style.display = 'none';

        // Hiển thị lại profile khi tab Profile được chọn
        if (this.id === 'profile-tab') {
            document.querySelector('#profile').style.display = 'block';
        }
    });
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
                throw new Error(`Failed to fetch group details for ID ${groupId}, Status: ${response.status}`);
            }
            return response.json();  // Nếu phản hồi hợp lệ, chuyển đổi thành JSON
        })
        .then(data => {
            if (data.status) {
                return data.data.group_name;  // Giả sử dữ liệu trả về có groupName
            } else {
                console.error("Failed to fetch group details:", data.message);
                return null;
            }
        })
        .catch(error => {
            console.error("Error:", error);
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
                throw new Error(`Failed to fetch user group IDs, Status: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            const groupIds = data.data; // Mảng các groupId
            const groupTableBody = document.getElementById('joined-groups-table-body');

            groupTableBody.innerHTML = ''; // Xóa các phần tử cũ trước khi thêm mới

            if (!groupIds || groupIds.length === 0) {
                const noGroupsRow = document.createElement('tr');
                noGroupsRow.innerHTML = '<td colspan="3" class="text-center">User has not joined any groups</td>';
                groupTableBody.appendChild(noGroupsRow);
                return;
            }

            // Lấy tên các nhóm dựa trên groupId
            const groupNamesPromises = groupIds.map(groupId => getGroupDetails(groupId));

            Promise.all(groupNamesPromises)
                .then(groupNames => {
                    groupNames.forEach((groupName, index) => {
                        if (groupName) {
                            const row = document.createElement('tr');
                            row.innerHTML = `
                                <th scope="row">${index + 1}</th>
                                <td>${groupName}</td>
                                <td>
                                    <button class="btn btn-outline-info btn-sm">View</button>
                                    <button class="btn btn-outline-danger btn-sm">Leave</button>
                                </td>
                            `;
                            groupTableBody.appendChild(row);
                        }
                    });
                })
                .catch(error => {
                    console.error('Error fetching group names:', error);
                });
        })
        .catch(error => {
            console.error('Error fetching user joined groups:', error);
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
                throw new Error(`Failed to fetch created groups, Status: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            const groupIds = data.data; // Mảng groupId
            const groupDetailsPromises = groupIds.map(groupId => getGroupDetails(groupId)); // Gọi API chi tiết group

            Promise.all(groupDetailsPromises)
                .then(groupNames => {
                    const groupTableBody = document.getElementById('created-groups-table-body');
                    groupTableBody.innerHTML = ''; // Xóa danh sách cũ

                    // Nếu không có nhóm
                    if (groupNames.length === 0) {
                        const noGroupsRow = document.createElement('tr');
                        noGroupsRow.innerHTML = '<td colspan="3" class="text-center">The user has not created any groups</td>';
                        groupTableBody.appendChild(noGroupsRow);
                    } else {
                        groupNames.forEach((groupName, index) => {
                            if (groupName) {
                                const row = document.createElement('tr');
                                row.innerHTML = `
                                    <th scope="row">${index + 1}</th>
                                    <td>${groupName}</td>
                                    <td>
                                        <button class="btn btn-outline-info btn-sm">View</button>
                                        <button class="btn btn-outline-danger btn-sm">Delete</button>
                                    </td>
                                `;
                                groupTableBody.appendChild(row);
                            }
                        });
                    }
                })
                .catch(error => {
                    console.error('Error fetching group details:', error);
                });
        })
        .catch(error => {
            console.error('Error fetching user-created groups:', error);
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
                throw new Error(`Failed to fetch friends list: ${response.status}`);
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
                    <td>${friend.status || 'Online'}</td>
                `;
                    friendsList.appendChild(row);
                });
            } else {
                friendsList.innerHTML = '<tr><td colspan="4" class="text-center">No friends available</td></tr>';
            }
        })
        .catch(error => {
            console.error('Error fetching friends list:', error);
            document.getElementById('friendsList').innerHTML = '<tr><td colspan="4" class="text-center">Error loading friends list</td></tr>';
        });
}





