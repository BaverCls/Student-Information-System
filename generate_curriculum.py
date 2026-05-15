import os

courses = {}
curriculums = []

def add_course(code, name, credit, quota, instructor, sem, dept):
    if code not in courses:
        courses[code] = f"{code},{name},{credit},{quota},{instructor},{sem},{dept}"

def add_to_curr(dept, sem, code):
    curriculums.append(f"{dept},{sem},{code}")

# Common Courses
add_course("MATH101", "Calculus I", 6, 100, "system", 1, "MATH")
add_course("PHYS101", "Physics I", 6, 80, "system", 1, "PHYS")
add_course("ENG101", "English I", 3, 100, "system", 1, "ENG")
add_course("HIST101", "History of Revolution", 2, 200, "system", 1, "HIST")
add_course("TURK101", "Turkish I", 1, 200, "system", 1, "TURK")
add_course("ECON101", "Economics 101", 3, 150, "alicebrown", 2, "ECON")
add_course("MATH102", "Calculus II", 6, 100, "system", 2, "MATH")
add_course("PHYS102", "Physics II", 6, 80, "system", 2, "PHYS")
add_course("ENG102", "English II", 3, 100, "system", 2, "ENG")
add_course("MATH201", "Linear Algebra", 6, 100, "system", 3, "MATH")
add_course("MATH202", "Differential Equations", 6, 100, "system", 4, "MATH")
add_course("BA202", "Global Trading", 3, 100, "alicebrown", 4, "BA")
add_course("CE204", "Tech Writing", 3, 100, "nazifecevik", 4, "CE")
add_course("ETHIC301", "Engineering Ethics", 3, 150, "system", 5, "ENG")
add_course("INNO302", "Innovation and Entrepreneurship", 3, 150, "system", 6, "ENG")
add_course("BA301", "Engineering Economics", 6, 60, "alicebrown", 6, "BA")

# CE 
ce_courses = [
    # Sem 1
    ("CS101", "Intro to Programming", 6, 1), ("CE102", "Discrete Math", 6, 1),
    # Sem 2
    ("CS102", "Object Oriented Programming", 6, 2), ("CE202", "Database Systems", 6, 2),
    # Sem 3
    ("CS201", "Data Structures", 6, 3), ("CE302", "Operating Systems", 6, 3), ("CE303", "Software Engineering", 6, 3), ("EE202", "Logic Design", 6, 3),
    # Sem 4
    ("CS202", "Computer Architecture", 6, 4), ("CE402", "Computer Networks", 6, 4), ("IE202", "Probability and Statistics", 6, 4),
    # Sem 5
    ("CS301", "Algorithms", 6, 5), ("CE501", "Web Development", 6, 5), ("CE503", "Formal Languages", 6, 5), ("CE_TECHSEL1", "Technical Selective I", 6, 5), ("CE_SEL1", "Selective I", 3, 5),
    # Sem 6
    ("CS302", "Artificial Intelligence", 6, 6), ("CE502", "Mobile App Development", 6, 6), ("CE_TECHSEL2", "Technical Selective II", 6, 6), ("CE_SEL2", "Selective II", 3, 6),
    # Sem 7
    ("CS401", "Machine Learning", 6, 7), ("CE601", "Graduation Project I", 12, 7), ("CE_TECHSEL3", "Technical Selective III", 6, 7), ("CE_TECHSEL4", "Technical Selective IV", 6, 7),
    # Sem 8
    ("CS402", "Computer Graphics", 6, 8), ("CE602", "Graduation Project II", 12, 8), ("CE_TECHSEL5", "Technical Selective V", 6, 8), ("CE_TECHSEL6", "Technical Selective VI", 6, 8)
]
for code, name, cr, sem in ce_courses:
    dept_val = "EE" if code.startswith("EE") else "IE" if code.startswith("IE") else "CE"
    add_course(code, name, cr, 50, "nazifecevik", sem, dept_val)

ce_curriculum = {
    1: ["CS101", "CE102", "MATH101", "PHYS101", "ENG101", "HIST101", "TURK101"],
    2: ["CS102", "CE202", "MATH102", "PHYS102", "ENG102", "ECON101"],
    3: ["CS201", "CE302", "CE303", "EE202", "MATH201"],
    4: ["CS202", "CE402", "MATH202", "IE202", "BA202", "CE204"],
    5: ["CS301", "CE501", "CE503", "CE_TECHSEL1", "CE_SEL1", "ETHIC301"],
    6: ["CS302", "CE502", "BA301", "CE_TECHSEL2", "CE_SEL2", "INNO302"],
    7: ["CS401", "CE601", "CE_TECHSEL3", "CE_TECHSEL4"],
    8: ["CS402", "CE602", "CE_TECHSEL5", "CE_TECHSEL6"]
}

# EE
ee_courses = [
    # 1
    ("EE101", "Intro to EE", 6, 1), ("EE103", "Circuit Analysis I", 6, 1),
    # 2
    ("EE102", "Digital Logic Design", 6, 2), ("EE104", "Circuit Analysis II", 6, 2),
    # 3
    ("EE201", "Signals and Systems", 6, 3), ("EE203", "Electromagnetic Theory", 6, 3), ("EE205", "Electronics I", 6, 3), ("EE_SEL1", "EE Selective I", 6, 3),
    # 4
    ("EE202", "Microprocessors", 6, 4), ("EE204", "Power Systems", 6, 4), ("EE206", "Electronics II", 6, 4),
    # 5
    ("EE301", "Control Systems", 6, 5), ("EE303", "Communication Systems", 6, 5), ("EE_TECHSEL1", "Technical Selective I", 6, 5), ("EE_TECHSEL2", "Technical Selective II", 6, 5), ("EE_SEL2", "Selective II", 3, 5),
    # 6
    ("EE302", "Digital Signal Processing", 6, 6), ("EE304", "Power Electronics", 6, 6), ("EE_TECHSEL3", "Technical Selective III", 6, 6), ("EE_TECHSEL4", "Technical Selective IV", 6, 6), ("EE_SEL3", "Selective III", 3, 6),
    # 7
    ("EE401", "Graduation Project I", 12, 7), ("EE_TECHSEL5", "Technical Selective V", 6, 7), ("EE_TECHSEL6", "Technical Selective VI", 6, 7), ("EE_TECHSEL7", "Technical Selective VII", 6, 7),
    # 8
    ("EE402", "Graduation Project II", 12, 8), ("EE_TECHSEL8", "Technical Selective VIII", 6, 8), ("EE_TECHSEL9", "Technical Selective IX", 6, 8), ("EE_TECHSEL10", "Technical Selective X", 6, 8)
]
for code, name, cr, sem in ee_courses:
    if code not in courses:
        add_course(code, name, cr, 50, "alicebrown", sem, "EE")

ee_curriculum = {
    1: ["EE101", "EE103", "MATH101", "PHYS101", "ENG101", "HIST101", "TURK101"],
    2: ["EE102", "EE104", "MATH102", "PHYS102", "ENG102", "ECON101"],
    3: ["EE201", "EE203", "EE205", "EE_SEL1", "MATH201"],
    4: ["EE202", "EE204", "EE206", "MATH202", "BA202", "CE204"],
    5: ["EE301", "EE303", "EE_TECHSEL1", "EE_TECHSEL2", "EE_SEL2", "ETHIC301"],
    6: ["EE302", "EE304", "EE_TECHSEL3", "EE_TECHSEL4", "EE_SEL3", "INNO302"],
    7: ["EE401", "EE_TECHSEL5", "EE_TECHSEL6", "EE_TECHSEL7"],
    8: ["EE402", "EE_TECHSEL8", "EE_TECHSEL9", "EE_TECHSEL10"]
}

# IE
ie_courses = [
    ("IE101", "Intro to IE", 6, 1), ("IE103", "Computer Aided Drawing", 6, 1),
    ("IE102", "Operations Research I", 6, 2), ("IE104", "Manufacturing Processes", 6, 2),
    ("IE201", "Operations Research II", 6, 3), ("IE203", "Work Study", 6, 3), ("IE205", "Engineering Economics", 6, 3), ("IE_SEL1", "IE Selective I", 6, 3),
    ("IE202", "Probability and Statistics", 6, 4), ("IE204", "Systems Engineering", 6, 4), ("IE206", "Simulation", 6, 4),
    ("IE301", "Production Planning", 6, 5), ("IE303", "Quality Control", 6, 5), ("IE_TECHSEL1", "Technical Selective I", 6, 5), ("IE_TECHSEL2", "Technical Selective II", 6, 5), ("IE_SEL2", "Selective II", 3, 5),
    ("IE302", "Supply Chain Management", 6, 6), ("IE304", "Facility Layout", 6, 6), ("IE_TECHSEL3", "Technical Selective III", 6, 6), ("IE_TECHSEL4", "Technical Selective IV", 6, 6), ("IE_SEL3", "Selective III", 3, 6),
    ("IE401", "Graduation Project I", 12, 7), ("IE_TECHSEL5", "Technical Selective V", 6, 7), ("IE_TECHSEL6", "Technical Selective VI", 6, 7), ("IE_TECHSEL7", "Technical Selective VII", 6, 7),
    ("IE402", "Graduation Project II", 12, 8), ("IE_TECHSEL8", "Technical Selective VIII", 6, 8), ("IE_TECHSEL9", "Technical Selective IX", 6, 8), ("IE_TECHSEL10", "Technical Selective X", 6, 8)
]
for code, name, cr, sem in ie_courses:
    if code not in courses:
        add_course(code, name, cr, 50, "janesmith", sem, "IE")

ie_curriculum = {
    1: ["IE101", "IE103", "MATH101", "PHYS101", "ENG101", "HIST101", "TURK101"],
    2: ["IE102", "IE104", "MATH102", "PHYS102", "ENG102", "ECON101"],
    3: ["IE201", "IE203", "IE205", "IE_SEL1", "MATH201"],
    4: ["IE202", "IE204", "IE206", "MATH202", "BA202", "CE204"],
    5: ["IE301", "IE303", "IE_TECHSEL1", "IE_TECHSEL2", "IE_SEL2", "ETHIC301"],
    6: ["IE302", "IE304", "IE_TECHSEL3", "IE_TECHSEL4", "IE_SEL3", "INNO302"],
    7: ["IE401", "IE_TECHSEL5", "IE_TECHSEL6", "IE_TECHSEL7"],
    8: ["IE402", "IE_TECHSEL8", "IE_TECHSEL9", "IE_TECHSEL10"]
}

# ME
me_courses = [
    ("ME101", "Intro to ME", 6, 1), ("ME103", "Computer Aided Drawing", 6, 1),
    ("ME102", "Statics", 6, 2), ("ME104", "Dynamics", 6, 2),
    ("ME201", "Thermodynamics I", 6, 3), ("ME203", "Fluid Mechanics I", 6, 3), ("ME205", "Material Science", 6, 3), ("ME_SEL1", "ME Selective I", 6, 3),
    ("ME202", "Thermodynamics II", 6, 4), ("ME204", "Fluid Mechanics II", 6, 4), ("ME206", "Manufacturing Processes", 6, 4),
    ("ME301", "Heat Transfer", 6, 5), ("ME303", "Machine Elements I", 6, 5), ("ME_TECHSEL1", "Technical Selective I", 6, 5), ("ME_TECHSEL2", "Technical Selective II", 6, 5), ("ME_SEL2", "Selective II", 3, 5),
    ("ME302", "Machine Elements II", 6, 6), ("ME304", "Control Systems", 6, 6), ("ME_TECHSEL3", "Technical Selective III", 6, 6), ("ME_TECHSEL4", "Technical Selective IV", 6, 6), ("ME_SEL3", "Selective III", 3, 6),
    ("ME401", "Graduation Project I", 12, 7), ("ME_TECHSEL5", "Technical Selective V", 6, 7), ("ME_TECHSEL6", "Technical Selective VI", 6, 7), ("ME_TECHSEL7", "Technical Selective VII", 6, 7),
    ("ME402", "Graduation Project II", 12, 8), ("ME_TECHSEL8", "Technical Selective VIII", 6, 8), ("ME_TECHSEL9", "Technical Selective IX", 6, 8), ("ME_TECHSEL10", "Technical Selective X", 6, 8)
]
for code, name, cr, sem in me_courses:
    if code not in courses:
        add_course(code, name, cr, 50, "bobmiller", sem, "ME")

me_curriculum = {
    1: ["ME101", "ME103", "MATH101", "PHYS101", "ENG101", "HIST101", "TURK101"],
    2: ["ME102", "ME104", "MATH102", "PHYS102", "ENG102", "ECON101"],
    3: ["ME201", "ME203", "ME205", "ME_SEL1", "MATH201"],
    4: ["ME202", "ME204", "ME206", "MATH202", "BA202", "CE204"],
    5: ["ME301", "ME303", "ME_TECHSEL1", "ME_TECHSEL2", "ME_SEL2", "ETHIC301"],
    6: ["ME302", "ME304", "ME_TECHSEL3", "ME_TECHSEL4", "ME_SEL3", "INNO302"],
    7: ["ME401", "ME_TECHSEL5", "ME_TECHSEL6", "ME_TECHSEL7"],
    8: ["ME402", "ME_TECHSEL8", "ME_TECHSEL9", "ME_TECHSEL10"]
}

# BA
ba_courses = [
    ("BA101", "Intro to Business", 6, 1), ("BA103", "Financial Accounting", 6, 1),
    ("BA102", "Management Info Systems", 6, 2), ("BA104", "Managerial Accounting", 6, 2),
    ("BA201", "Microeconomics", 6, 3), ("BA203", "Marketing Management", 6, 3), ("BA205", "Org Behavior", 6, 3), ("BA_SEL1", "BA Selective I", 6, 3),
    ("BA204", "Macroeconomics", 6, 4), ("BA206", "Human Resources", 6, 4),
    ("BA301", "Corporate Finance", 6, 5), ("BA303", "Operations Management", 6, 5), ("BA_TECHSEL1", "Technical Selective I", 6, 5), ("BA_TECHSEL2", "Technical Selective II", 6, 5), ("BA_SEL2", "Selective II", 3, 5),
    ("BA302", "Strategic Management", 6, 6), ("BA304", "Business Ethics", 6, 6), ("BA_TECHSEL3", "Technical Selective III", 6, 6), ("BA_TECHSEL4", "Technical Selective IV", 6, 6), ("BA_SEL3", "Selective III", 3, 6),
    ("BA401", "Graduation Project I", 12, 7), ("BA_TECHSEL5", "Technical Selective V", 6, 7), ("BA_TECHSEL6", "Technical Selective VI", 6, 7), ("BA_TECHSEL7", "Technical Selective VII", 6, 7),
    ("BA402", "Graduation Project II", 12, 8), ("BA_TECHSEL8", "Technical Selective VIII", 6, 8), ("BA_TECHSEL9", "Technical Selective IX", 6, 8), ("BA_TECHSEL10", "Technical Selective X", 6, 8)
]
for code, name, cr, sem in ba_courses:
    if code not in courses:
        add_course(code, name, cr, 50, "alicebrown", sem, "BA")

ba_curriculum = {
    1: ["BA101", "BA103", "MATH101", "PHYS101", "ENG101", "HIST101", "TURK101"],
    2: ["BA102", "BA104", "MATH102", "PHYS102", "ENG102", "ECON101"],
    3: ["BA201", "BA203", "BA205", "BA_SEL1", "MATH201"],
    4: ["BA204", "BA206", "MATH202", "IE202", "BA202", "CE204"],
    5: ["BA301", "BA303", "BA_TECHSEL1", "BA_TECHSEL2", "BA_SEL2", "ETHIC301"],
    6: ["BA302", "BA304", "BA_TECHSEL3", "BA_TECHSEL4", "BA_SEL3", "INNO302"],
    7: ["BA401", "BA_TECHSEL5", "BA_TECHSEL6", "BA_TECHSEL7"],
    8: ["BA402", "BA_TECHSEL8", "BA_TECHSEL9", "BA_TECHSEL10"]
}

# Add all to curriculums list
for sem, curr_list in ce_curriculum.items():
    for c in curr_list:
        add_to_curr("CE", sem, c)
        
for sem, curr_list in ee_curriculum.items():
    for c in curr_list:
        add_to_curr("EE", sem, c)

for sem, curr_list in ie_curriculum.items():
    for c in curr_list:
        add_to_curr("IE", sem, c)

for sem, curr_list in me_curriculum.items():
    for c in curr_list:
        add_to_curr("ME", sem, c)

for sem, curr_list in ba_curriculum.items():
    for c in curr_list:
        add_to_curr("BA", sem, c)

with open("data/courses.txt", "w") as f:

    for c in courses.values():
        f.write(c + "\n")

with open("data/curriculums.txt", "w") as f:
    for c in curriculums:
        f.write(c + "\n")

print("Files generated successfully.")
